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
    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                 Active view: -
                 
                 no documents
                 no views
                 
                 """.trimIndent())
        val currentPath = Paths.get("").toAbsolutePath()

        // add a sourcefile and a view definition
        val tagFilename = createTagmlFileName("transcription")
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath))
        val viewName = "l"
        val viewFilename = createViewFileName(viewName)
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        val viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath))
        success = cli!!.run(command)
        assertSucceedsWithExpectedStdout(
                success,
                """Active view: -

no documents
no views

Uncommitted changes:
  (use "alexandria commit <file>..." to commit the selected changes)
  (use "alexandria commit -a" to commit all changes)
  (use "alexandria revert <file>..." to discard changes)

        modified: $tagPathRelativeToCurrentDir
        modified: $viewPathRelativeToCurrentDir""")

        // commit files
        runCommitAllCommand()
        success = cli!!.run(command)
        softlyAssertSucceedsWithStdoutContaining(success, "Active view: -\n")

        // checkout view
        runCheckoutCommand(viewName)
        success = cli!!.run(command)
        val cliContext2 = readCLIContext()
        val lastCommit = cliContext2.watchedFiles[tagFilename]!!.lastCommit
        val lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant()
        assertThat(lastCommit.isAfter(lastModified))
        softlyAssertSucceedsWithStdoutContaining(success, "Active view: l\n")

        // checkout main view and change a file
        runCheckoutCommand("-")
        val newTagml = "[tagml>something else<tagml]"
        modifyFile(tagFilename, newTagml)
        success = cli!!.run(command)
        softlyAssertSucceedsWithStdoutContaining(
                success,
                "Active view: -\n"
        )

        // delete file
        deleteFile(tagFilename)
        success = cli!!.run(command)
        softlyAssertSucceedsWithStdoutContaining(
                success,
                "Active view: -\n"
        )
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testTagmlFileInRootShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     
     """.trimIndent())
        val currentPath = Paths.get("").toAbsolutePath()
        val tagFilename = "transcription.tag"
        val tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath))
        success = cli!!.run(command)
        softlyAssertSucceedsWithStdoutContaining(
                success,
                """Untracked files:
  (use "alexandria add <file>..." to start tracking this file.)

        $tagPathRelativeToCurrentDir""")
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testJsonFileInRootShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                Active view: -
                
                no documents
                no views
                
                """.trimIndent())
        val currentPath = Paths.get("").toAbsolutePath()
        val viewFilename = "viewdef.json"
        val viewPath = createFile(viewFilename, "{}")
        val viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath))
        success = cli!!.run(command)
        softlyAssertSucceedsWithStdoutContaining(
                success,
                """Untracked files:
  (use "alexandria add <file>..." to start tracking this file.)

        $viewPathRelativeToCurrentDir""")
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testOtherFileInRootNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     
     """.trimIndent())
        val otherFilename = "other.md"
        createFile(otherFilename, "bla bla bla")
        success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     """.trimIndent())
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testDirectoryWithTagmlFileShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     
     """.trimIndent())
        val currentPath = Paths.get("").toAbsolutePath()
        val directoryName = "subdir"
        val directoryPath = createDirectory(directoryName)
        val directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath))
        val tagFilename = "subdir/transcription.tag"
        createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """Active view: -

no documents
no views


Untracked files:
  (use "alexandria add <file>..." to start tracking this file.)

        $directoryPathRelativeToCurrentDir""")
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testDirectoryStartingWithPointWithTagmlFileNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     
     """.trimIndent())
        val currentPath = Paths.get("").toAbsolutePath()
        val directoryName = ".subdir"
        val directoryPath = createDirectory(directoryName)
        val directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath))
        val tagFilename = ".subdir/transcription.tag"
        createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     """.trimIndent())
    }

    @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
    @Test
    @Throws(Exception::class)
    fun testDirectoryWithoutTagmlOrJsonFileNotShownAsUntrackedFile() {
        runInitCommand()

        // in an empty, initialized directory
        var success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     
     """.trimIndent())
        val directoryName = "subdir"
        createDirectory(directoryName)
        val otherFilename = "subdir/other.txt"
        createFile(otherFilename, "Don't feed them after dark.")
        success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success, """
     Active view: -
     
     no documents
     no views
     """.trimIndent())
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       status [-h]

Show the directory status (active view, modified files, etc.).

named arguments:
  -h, --help             show this help message and exit""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command)
    }

    companion object {
        private val command = StatusCommand().name
    }
}
