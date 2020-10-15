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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ExportRenderedDotCommand
import org.assertj.core.api.SoftAssertions
import org.junit.Test

class ExportSvgCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        val softly = SoftAssertions()
        softly.assertThat(success).`as`("Exit success").isTrue
        val svgContent = stdOut.toString()
        softly.assertThat(svgContent).`as`("stdout").isNotEmpty
        softly.assertThat(svgContent).`as`("stdoutIsSvg").contains("<svg ")
        softly.assertThat(stdErr.toString().trim { it <= ' ' }).`as`("stderr").isEmpty()
        softly.assertAll()
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       export-svg [-o <file>] [-h] <document>

Export the document as svg. (Requires access to Graphviz' dot command)

positional arguments:
  <document>             The name of the document to export.

named arguments:
  -o <file>, --outputfile <file>
                         The file to export to.
  -h, --help             show this help message and exit""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "document")
    }

    companion object {
        private val command = ExportRenderedDotCommand("svg").name
    }
}
