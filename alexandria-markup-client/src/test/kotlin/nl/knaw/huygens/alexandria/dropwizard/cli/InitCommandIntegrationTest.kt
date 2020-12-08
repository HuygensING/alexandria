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
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.InitCommand
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.StatusCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InitCommandIntegrationTest : CommandIntegrationTest() {

    @Test
    @Throws(Exception::class)
    fun init_command_autocommits_files_in_watched_dirs() {
        createDirectory("tagml")
        val tagFilename = createTagmlFileName("transcription1")
        createFile(tagFilename, "[tagml>[l>test<l]<tagml]")
        val success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
            success,
            """
            |initializing...
            |  mkdir ${workDirectory!!.resolve(".alexandria")}
            |  mkdir ${workDirectory!!.resolve("tagml")}
            |  mkdir ${workDirectory!!.resolve("views")}
            |  mkdir ${workDirectory!!.resolve("sparql")}
            |Parsing tagml/transcription1.tagml to document transcription1...
            |done!
            |""".trimMargin()
        )
        val status = cli!!.run(StatusCommand().name)
        assertThat(status).isTrue
        val normalizedStdOut = stdOut.toString().normalized()
        assertThat(normalizedStdOut).contains("Documents:", "source: tagml/transcription1.tagml")
        assertThat(normalizedStdOut).doesNotContain("commit", "Modified files")
    }

    @Test
    @Throws(Exception::class)
    fun init_command_fails_when_current_dir_is_not_writable() {
        val asFile = workDirectory!!.resolve(".alexandria").toFile().createNewFile()
        assertThat(asFile).isTrue
        val success = cli!!.run(command)
        assertFailsWithExpectedStderr(
            success, "init failed: could not create directory " + workDirectory!!.resolve(".alexandria")
        )
    }

    @Test
    @Throws(Exception::class)
    fun init_command_has_expected_result() {
        val success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
            success,
            """
            |initializing...
            |  mkdir ${workDirectory!!.resolve(".alexandria")}
            |  mkdir ${workDirectory!!.resolve("tagml")}
            |  mkdir ${workDirectory!!.resolve("views")}
            |  mkdir ${workDirectory!!.resolve("sparql")}
            |done!
            |""".trimMargin()
        )
        val viewsDir = workFilePath(AlexandriaCommand.VIEWS_DIR)
        assertThat(viewsDir).isDirectory.isWritable
        val transcriptionsDir = workFilePath(AlexandriaCommand.SOURCE_DIR)
        assertThat(transcriptionsDir).isDirectory.isWritable
        val sparqlDir = workFilePath(AlexandriaCommand.SPARQL_DIR)
        assertThat(sparqlDir).isDirectory.isWritable
        val cliContext = readCLIContext()
        assertThat(cliContext.activeView).isEqualTo("-")
    }

    @Test
    @Throws(Exception::class)
    fun init_command_help_has_expected_output() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
            success,
            """
            |usage: java -jar alexandria-app.jar
            |       init [-h]
            |
            |Initializes current directory as an alexandria workspace.
            |
            |named arguments:
            |  -h, --help             show this help message and exit
            |""".trimMargin()
        )
    }

    companion object {
        private val command = InitCommand().name
    }
}
