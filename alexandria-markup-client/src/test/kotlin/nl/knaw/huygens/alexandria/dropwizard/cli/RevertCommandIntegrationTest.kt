package nl.knaw.huygens.alexandria.dropwizard.cli

/*-
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2021 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.RevertCommand
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.StatusCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RevertCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testRevertOfViewDefinition() {
        runInitCommand()

        // create sourcefile
        val viewFilename = createViewFileName("v1")
        val json = "{\"excludeMarkup\":[\"l\"]}"
        val viewDefinitionPath = createFile(viewFilename, json)
        runAddCommand(viewDefinitionPath)
        runCommitAllCommand()

        // overwrite sourcefile
        val json2 = "{\"excludeMarkup\":[\"blablabla\"]}"
        modifyFile(viewDefinitionPath, json2)
        val fileContentsBeforeRevert = readFileContents(viewFilename)
        assertThat(fileContentsBeforeRevert).isEqualTo(json2)
        val success = cli!!.run(command, viewDefinitionPath)
        assertSucceedsWithExpectedStdout(success, "Reverting $viewFilename...\ndone!")
        val fileContentsAfterRevert = readFileContents(viewFilename)
        assertThat(fileContentsAfterRevert).isEqualTo(json)
        val statusSuccess = cli!!.run(StatusCommand().name)
        assertThat(statusSuccess).isTrue
        val stdOut = stdOut.toString().normalized()
        assertThat(stdOut).doesNotContain("modified: $viewFilename")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandInMainView() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()

        // overwrite sourcefile
        val tagml2 = "[x>And now for something completely different.<x]"
        modifyFile(tagFilename, tagml2)
        val fileContentsBeforeRevert = readFileContents(tagFilename)
        assertThat(fileContentsBeforeRevert).isEqualTo(tagml2)
        val success = cli!!.run(command, tagPath)
        assertSucceedsWithExpectedStdout(success, "Reverting $tagFilename...\ndone!")
        val fileContentsAfterRevert = readFileContents(tagFilename)
        assertThat(fileContentsAfterRevert).isEqualTo(tagml)
        val statusSuccess = cli!!.run(StatusCommand().name)
        assertThat(statusSuccess).isTrue
        val stdOut = stdOut.toString().normalized()
        assertThat(stdOut).doesNotContain("modified: tagml/transcription.tagml")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandInView() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription1")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)

        // create viewfile
        val viewName = "l"
        val viewFilename = createViewFileName(viewName)
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(tagPath, viewPath)
        runCommitAllCommand()
        runCheckoutCommand(viewName)
        val tagml_l = "[l>test<l]"

        // overwrite sourcefile
        val tagml2 = "[x>And now for something completely different.<x]"
        modifyFile(tagFilename, tagml2)
        val fileContentsBeforeRevert = readFileContents(tagFilename)
        assertThat(fileContentsBeforeRevert).isEqualTo(tagml2)
        val success = cli!!.run(command, tagPath)
        assertSucceedsWithExpectedStdout(success, "Reverting $tagFilename...\ndone!")
        val fileContentsAfterRevert = readFileContents(tagFilename)
        assertThat(fileContentsAfterRevert).isEqualTo(tagml_l)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       revert [-h] <file> [<file> ...]

Restore the document file(s).

positional arguments:
  <file>                 the file to be reverted

named arguments:
  -h, --help             show this help message and exit""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "something")
    }

    companion object {
        private val command = RevertCommand().name
    }
}
