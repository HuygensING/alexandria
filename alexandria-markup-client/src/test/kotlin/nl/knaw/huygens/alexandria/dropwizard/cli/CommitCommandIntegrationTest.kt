package nl.knaw.huygens.alexandria.dropwizard.cli


/*-
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CommitCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test

class CommitCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommandWithBadViewDefinitionThrowsError() {
        runInitCommand()
        val viewFilename = createViewFileName("v1")
        val viewPath = createFile(viewFilename, "{\"idontknowwhatimdoing\":[\"huh?\"]}")
        runAddCommand(viewPath)
        val success = cli!!.run(command, "-a")
        softlyAssertFailsWithExpectedStderr(
                success,
                "Commit aborted: Invalid view definition in views/v1.json: none of the allowed options includeLayers, excludeLayers, includeMarkup or excludeMarkup was found.")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithoutFileThrowsError() {
        runInitCommand()
        val success = cli!!.run(command)
        softlyAssertFailsWithExpectedStderr(
                success, "Commit aborted: no files specified. Use -a to commit all changed tracked files.")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithFile() {
        runInitCommand()
        val filename = "transcription1.tagml"
        val absolutePath = createFile(filename, "[tagml>test<tagml]")
        runAddCommand(absolutePath)
        val dateAfterAdd = readLastCommittedInstant(filename)
        assertThat(dateAfterAdd).isNotNull
        logger.info("{}", dateAfterAdd)
        val success = cli!!.run(command, absolutePath)
        softlyAssertSucceedsWithExpectedStdout(
                success, "Parsing transcription1.tagml to document transcription1...\ndone!")
        val dateAfterCommit = readLastCommittedInstant(filename)
        assertThat(dateAfterCommit).isAfter(dateAfterAdd)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithAllOption() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription1")
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val viewFilename = createViewFileName("v1")
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(tagPath, viewPath)
        val tagDateAfterAdd = readLastCommittedInstant(tagFilename)
        assertThat(tagDateAfterAdd).isNotNull
        val viewDateAfterAdd = readLastCommittedInstant(viewFilename)
        assertThat(viewDateAfterAdd).isNotNull
        val success = cli!!.run(command, "-a")
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                    Parsing tagml/transcription1.tagml to document transcription1...
                    Parsing views/v1.json to view v1...
                    done!
                    """.trimIndent())
        val tagDateAfterCommit = readLastCommittedInstant(tagFilename)
        assertThat(tagDateAfterCommit).isAfter(tagDateAfterAdd)
        val viewDateAfterCommit = readLastCommittedInstant(viewFilename)
        assertThat(viewDateAfterCommit).isAfter(viewDateAfterAdd)
    }

    @Ignore("fails on jenkins")
    @Test
    @Throws(Exception::class)
    fun testCommitWithActiveView() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription1")
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val viewFilename = createViewFileName("v1")
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(tagPath, viewPath)
        val tagDateAfterAdd = readLastCommittedInstant(tagFilename)
        assertThat(tagDateAfterAdd).isNotNull
        val viewDateAfterAdd = readLastCommittedInstant(viewFilename)
        assertThat(viewDateAfterAdd).isNotNull
        val success = cli!!.run(command, "-a")
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                    Parsing tagml/transcription1.tagml to document transcription1...
                    Parsing views/v1.json to view v1...
                    done!
                    """.trimIndent())
        val tagDateAfterCommit = readLastCommittedInstant(tagFilename)
        assertThat(tagDateAfterCommit).isAfter(tagDateAfterAdd)
        val viewDateAfterCommit = readLastCommittedInstant(viewFilename)
        assertThat(viewDateAfterCommit).isAfter(viewDateAfterAdd)

        // alexandria checkout v1
        runCheckoutCommand("v1")

        // create new transcription & view
        val tagFilename2 = createTagmlFileName("transcription2")
        val tagPath2 = createFile(tagFilename2, "[tagml>[l>Hello World<l]<tagml]")
        val viewFilename2 = createViewFileName("v2")
        val viewPath2 = createFile(viewFilename2, "{\"includeMarkup\":[\"m\"]}")
        runAddCommand(tagPath2, viewPath2)
        modifyFile(tagFilename, "[tagml>[p>Hello world!<p]<tagml]")
        //    modifyFile(viewFilename, "{\"includeMarkup\":[\"p\"]}");

        //    final boolean success2 = cli.run("status");
        //    assertSucceedsWithExpectedStdout(success2, "");
        val success3 = cli!!.run(command, "-a")
        assertFailsWithExpectedStdoutAndStderr(
                success3,
                """
                Parsing tagml/transcription2.tagml to document transcription2...
                Parsing views/v2.json to view v2...
                """.trimIndent(),
                """unable to commit tagml/transcription1.tagml
View v1 is active. Currently, committing changes to existing documents is only allowed in the main view. Use:
  alexandria revert tagml/transcription1.tagml
  alexandria checkout -
to undo those changes and return to the main view.
some commits failed""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommittingANewViewDefinitionInAnOpenViewShouldWork() {
        runInitCommand()

        // setup: add a file and viewdefinition v1
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val absoluteTagmlPath = createFile(tagFilename, tagml)
        val viewName = "v1"
        val viewFilename = createViewFileName(viewName)
        val absoluteViewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(absoluteTagmlPath, absoluteViewPath)
        runCommitAllCommand()

        // checkout view v1
        runCheckoutCommand("v1")
        val viewName2 = "v2"
        val viewFilename2 = createViewFileName(viewName2)
        val absoluteViewPath2 = createFile(viewFilename2, "{\"excludeMarkup\":[\"l\"]}")
        runAddCommand(absoluteViewPath2)
        val dateAfterAdd = readLastCommittedInstant(viewFilename2)
        assertThat(dateAfterAdd).isNotNull
        logger.info("{}", dateAfterAdd)
        val success = cli!!.run(command, absoluteViewPath2)
        softlyAssertSucceedsWithExpectedStdout(success, "Parsing views/v2.json to view v2...\ndone!")
        val dateAfterCommit = readLastCommittedInstant(viewFilename2)
        assertThat(dateAfterCommit).isAfter(dateAfterAdd)
        val cliContext = readCLIContext()
        assertThat(cliContext.tagViewDefinitions.keys).containsOnly(viewName, viewName2)
    }

    @Test
    @Throws(Exception::class)
    fun testCommittingAModifiedViewDefinitionForTheCurrentlyOpenedViewShouldGiveAnError() {
        runInitCommand()

        // setup: add a file and viewdefinition v1
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val absoluteTagmlPath = createFile(tagFilename, tagml)
        val viewName = "v1"
        val viewFilename = createViewFileName(viewName)
        val absoluteViewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(absoluteTagmlPath, absoluteViewPath)
        runCommitAllCommand()

        // checkout view v1
        runCheckoutCommand("v1")
        modifyFile(viewFilename, "{\"excludeMarkup\":[\"l\"]}")
        runAddCommand(absoluteViewPath)
        val success = cli!!.run(command, absoluteViewPath)
        assertFailsWithExpectedStderr(
                success,
                """You are trying to modify the definition file $viewFilename of the active view $viewName. This is not allowed.

Use:
  alexandria revert views/v1.json
  alexandria checkout -
to undo those changes and return to the main view.
some commits failed""")
        val cliContext = readCLIContext()
        assertThat(cliContext.tagViewDefinitions.keys).containsOnly(viewName)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       commit [-a] [-h] [<file> [<file> ...]]

Record changes to the repository.

positional arguments:
  <file>                 the changed file(s)

named arguments:
  -a                     automatically  add  all  changed  files  (default:
                         false)
  -h, --help             show this help message and exit

Warning: currently, committing tagml changes  is  only possible in the main
view!""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "-a")
    }

    companion object {
        private val command = CommitCommand().name
    }
}
