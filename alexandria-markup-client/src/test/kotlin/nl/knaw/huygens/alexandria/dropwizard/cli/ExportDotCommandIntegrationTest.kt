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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ExportDotCommand
import org.junit.Test

class ExportDotCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val file = createFile(tagFilename, tagml)
        runAddCommand(file)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """digraph TextGraph{
  node [font="helvetica";style="filled";fillcolor="white"]
  d [shape=doublecircle;label=""]
  subgraph{
    t4 [shape=box;arrowhead=none;label=<#PCDATA<br/>test>]
    rank=same
  }
  m2 [color=red;label=<tagml>]
  m3 [color=red;label=<l>]
  m2->m3[color=red;arrowhead=none]
  m3->t4[color=red;arrowhead=none]
  d->m2 [arrowhead=none]
}""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandInView() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        val viewName = "l"
        val viewFilename = createViewFileName(viewName)
        val viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}")
        runAddCommand(tagPath, viewPath)
        runCommitAllCommand()
        runCheckoutCommand(viewName)
        val success = cli!!.run(command, "transcription")
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """digraph TextGraph{
  node [font="helvetica";style="filled";fillcolor="white"]
  d [shape=doublecircle;label=""]
  subgraph{
    t4 [shape=box;arrowhead=none;label=<#PCDATA<br/>test>]
    rank=same
  }
  m2 [color=red;label=<tagml>]
  m3 [color=red;label=<l>]
  m2->m3[color=red;arrowhead=none]
  m3->t4[color=red;arrowhead=none]
  d->m2 [arrowhead=none]
}""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       export-dot [-o <file>] [-h] <document>

Export the document as .dot file.

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
        private val command = ExportDotCommand().name
    }
}
