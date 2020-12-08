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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AddCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AddCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()
        val filename1 = "transcription1.tagml"
        val absolutePath1 = createFile(filename1, "")
        val filename2 = "transcription2.tagml"
        val absolutePath2 = createFile(filename2, "")
        val throwable = cli!!.run(command, absolutePath1, absolutePath2)
        softlyAssertSucceedsWithExpectedStdout(throwable, "")

        val cliContext = readCLIContext()
        assertThat(cliContext.watchedFiles.keys)
                .containsExactlyInAnyOrder(filename1, filename2)
        val fileInfo1 = cliContext.watchedFiles[filename1]!!
        assertThat(fileInfo1.fileType).isEqualTo(FileType.tagmlSource)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithNonExistingFilesFails() {
        runInitCommand()
        val throwable = cli!!.run(command, "transcription1.tagml", "transcription2.tagml")
        assertThat(throwable).isTrue
        assertThat(cliStdErrAsString)
                .contains("transcription1.tagml is not a file!")
                .contains("transcription2.tagml is not a file!")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithoutParametersFails() {
        val throwable = cli!!.run(command)
        assertThat(cliStdErrAsString).contains("too few arguments")
        assertFailsWithExpectedStderr(
                throwable,
                """
                too few arguments
                usage: java -jar alexandria-app.jar
                       add [-h] <file> [<file> ...]
                
                Add file context to the index.
                
                positional arguments:
                  <file>                 the files to watch
                
                named arguments:
                  -h, --help             show this help message and exit
                """.trimIndent())
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val throwable = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                throwable,
                """
                usage: java -jar alexandria-app.jar
                       add [-h] <file> [<file> ...]
                
                Add file context to the index.
                
                positional arguments:
                  <file>                 the files to watch
                
                named arguments:
                  -h, --help             show this help message and exit
                """.trimIndent())
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "something")
    }

    companion object {
        private val command = AddCommand().name
    }
}
