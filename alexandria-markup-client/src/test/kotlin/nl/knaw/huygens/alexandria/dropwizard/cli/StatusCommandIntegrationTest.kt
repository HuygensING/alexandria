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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.StatusCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.nio.file.Files
import java.nio.file.Paths

class StatusCommandIntegrationTest : CommandIntegrationTest() {

    //    @Ignore
    @Test
    fun testCommand() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )
        val currentPath = Paths.get("").toAbsolutePath()

        // add a sourcefile and a view definition
        val tagFilename = createTagmlFileName("transcription")
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath))
        val viewName = "l"
        val viewFilename = createViewFileName(viewName)
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        val viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath))

        assertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
            |Active view: -
            |
            |no documents
            |no views
            |
            |Uncommitted changes:
            |  (use "alexandria commit <file>..." to commit the selected changes)
            |  (use "alexandria commit -a" to commit all changes)
            |  (use "alexandria revert <file>..." to discard changes)
            |
            |        modified: $tagPathRelativeToCurrentDir
            |        modified: $viewPathRelativeToCurrentDir
            |""".trimMargin()
        )

        // commit files
        runCommitAllCommand()
        softlyAssertSucceedsWithStdoutContaining(cli!!.run(command), "Active view: -\n")

        // checkout view
        runCheckoutCommand(viewName)
        val success = cli!!.run(command)
        val cliContext2 = readCLIContext()
        val lastCommit = cliContext2.watchedFiles[tagFilename]!!.lastCommit
        val lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant()
        assertThat(lastCommit.isAfter(lastModified))
        softlyAssertSucceedsWithStdoutContaining(success, "Active view: l\n")

        // checkout main view and change a file
        runCheckoutCommand("-")
        val newTagml = "[tagml>something else<tagml]"
        modifyFile(tagFilename, newTagml)
        softlyAssertSucceedsWithStdoutContaining(
            cli!!.run(command),
            "Active view: -\n"
        )

        // delete file
        deleteFile(tagFilename)
        softlyAssertSucceedsWithStdoutContaining(
            cli!!.run(command),
            "Active view: -\n"
        )
    }

    @Test
    fun added_file_in_root_shows_as_modified() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )

        val currentPath = Paths.get("").toAbsolutePath()
        val tagFilename = "transcription.tag"
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath))
        val result = cli!!.run("add", tagPathRelativeToCurrentDir.toString())
        assertThat(result).isTrue

        softlyAssertSucceedsWithStdoutContaining(
            cli!!.run(command),
            """
            |Uncommitted changes:
            |  (use "alexandria commit <file>..." to commit the selected changes)
            |  (use "alexandria commit -a" to commit all changes)
            |  (use "alexandria revert <file>..." to discard changes)
            |
            |        modified: $tagPathRelativeToCurrentDir""".trimMargin()
        )
    }

    @Ignore
    @Test
    fun testTagmlFileInRootShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )

        resetCli()
        val currentPath = Paths.get("").toAbsolutePath()
        val tagFilename = "transcription.tag"
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath))
        softlyAssertSucceedsWithStdoutContaining(
            cli!!.run(command),
            """
            |Untracked files:
            |  (use "alexandria add <file>..." to start tracking this file.)
            |
            |        $tagPathRelativeToCurrentDir
            |""".trimMargin()
        )
    }

    @Ignore
    @Test
    fun testJsonFileInRootShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
            Active view: -
            
            no documents
            no views
            
            """.trimIndent()
        )

        val currentPath = Paths.get("").toAbsolutePath()
        val viewFilename = "viewdef.json"
        val viewPath = createFile(viewFilename, "{}")
        val viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath))
        softlyAssertSucceedsWithStdoutContaining(
            cli!!.run(command),
            """
            |Untracked files:
            |  (use "alexandria add <file>..." to start tracking this file.)
            |
            |        $viewPathRelativeToCurrentDir
            |""".trimMargin()
        )
    }

    //    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    fun testOtherFileInRootNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )
        val otherFilename = "other.md"
        createFile(otherFilename, "bla bla bla")
        resetCli()
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             """.trimIndent()
        )
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    fun testDirectoryWithTagmlFileShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )
        val currentPath = Paths.get("").toAbsolutePath()
        val directoryName = "subdir"
        val directoryPath = createDirectory(directoryName)
        val directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath))
        val tagFilename = "subdir/transcription.tag"
        createFile(tagFilename, "[tagml>[l>test<l]<tagml]")

        resetCli()
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
            |Active view: -
            |
            |no documents
            |no views
            |
            |
            |Untracked files:
            |  (use "alexandria add <file>..." to start tracking this file.)
            |
            |        $directoryPathRelativeToCurrentDir
            |""".trimMargin()
        )
    }

    //    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    fun testDirectoryStartingWithPointWithTagmlFileNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )
        val currentPath = Paths.get("").toAbsolutePath()
        val directoryName = ".subdir"
        val directoryPath = createDirectory(directoryName)
        val directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath))
        val tagFilename = ".subdir/transcription.tag"
        createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        resetCli()
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             """.trimIndent()
        )
    }

    //    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    fun testDirectoryWithoutTagmlOrJsonFileNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             
             """.trimIndent()
        )
        val directoryName = "subdir"
        createDirectory(directoryName)
        val otherFilename = "subdir/other.txt"
        createFile(otherFilename, "Don't feed them after dark.")
        resetCli()
        softlyAssertSucceedsWithExpectedStdout(
            cli!!.run(command),
            """
             Active view: -
             
             no documents
             no views
             """.trimIndent()
        )
    }

    @Test
    fun testCommandHelp() {
        assertSucceedsWithExpectedStdout(
            cli!!.run(command, "-h"),
            """
            |usage: java -jar alexandria-app.jar
            |       status [-h]
            |
            |Show the directory status (active view, modified files, etc.).
            |
            |named arguments:
            |  -h, --help             show this help message and exit
            |""".trimMargin()
        )
    }

    @Test
    fun status_command_should_be_run_in_an_initialized_directory() {
        assertCommandRunsInAnInitializedDirectory(command)
    }

    companion object {
        private val command = StatusCommand().name
    }
}
