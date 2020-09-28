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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CheckOutCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.nio.file.Files

class CheckOutCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val absoluteTagmlPath = createFile(tagFilename, tagml)
        val viewName = "v1"
        val viewFilename = createViewFileName(viewName)
        val absoluteViewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(absoluteTagmlPath, absoluteViewPath)
        runCommitAllCommand()
        val success = cli!!.run(command, viewName)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                |Checking out view v1...
                |  updating tagml/transcription.tagml...
                |done!
                """.trimMargin())

        val cliContext = readCLIContext()
        assertThat(cliContext.activeView).isEqualTo(viewName)

        val newContent = readFileContents(tagFilename)
        assertThat(newContent).isEqualTo("[l>test<l]")

        val success2 = cli!!.run(command, AlexandriaCommand.MAIN_VIEW)
        softlyAssertSucceedsWithExpectedStdout(
                success2,
                """
                |Checking out main view...
                |  updating tagml/transcription.tagml...
                |done!
                """.trimMargin())
        val cliContext2 = readCLIContext()
        assertThat(cliContext2.activeView).isEqualTo(AlexandriaCommand.MAIN_VIEW)

        val lastCommit = cliContext2.watchedFiles[tagFilename]!!.lastCommit
        val lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant()
        assertThat(lastCommit.isAfter(lastModified))

        val newContent2 = readFileContents(tagFilename)
        assertThat(newContent2).isEqualTo(tagml)
    }

    @Ignore("race condition? fails on Jenkins")
    @Test
    @Throws(Exception::class)
    fun testCheckoutNotPossibleWithUncommittedFilesPresent() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription1")
        val tagml = "[tagml>[l>test<l]<tagml]"
        createFile(tagFilename, tagml)
        val viewName = "v1"
        val viewFilename = createViewFileName(viewName)
        createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(tagFilename, viewFilename)
        runCommitAllCommand()
        val success = cli!!.run(command, viewName)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """Checking out view v1...
  updating tagml/transcription1.tagml...
done!""")
        val cliContext = readCLIContext()
        assertThat(cliContext.activeView).isEqualTo(viewName)
        val newContent = readFileContents(tagFilename)
        assertThat(newContent).isEqualTo("[l>test<l]")

        // now, change the file contents
        modifyFile(tagFilename, "[l>foo bar<l]")
        val success2 = cli!!.run(command, AlexandriaCommand.MAIN_VIEW)
        val stdOut = cliStdOutAsString.normalized()
        assertThat(stdOut)
                .isEqualTo(
                        """Uncommitted changes:
  (use "alexandria commit <file>..." to commit the selected changes)
  (use "alexandria commit -a" to commit all changes)
  (use "alexandria revert <file>..." to discard changes)

        modified: tagml/transcription1.tagml""")
        softlyAssertFailsWithExpectedStderr(
                success2, "Uncommitted changes found, cannot checkout another view.")
    }

    // On checkout, the lastcommitted dates should be adjusted.
    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       checkout [-h] <view>

Activate or deactivate a view in this directory.

positional arguments:
  <view>                 The name of the view to use

named arguments:
  -h, --help             show this help message and exit""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "-")
    }

    companion object {
        private val command = CheckOutCommand().name
    }
}
