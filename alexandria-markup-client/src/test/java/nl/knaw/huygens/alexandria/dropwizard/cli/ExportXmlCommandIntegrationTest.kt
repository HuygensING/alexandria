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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ExportXmlCommand
import org.junit.Test

class ExportXmlCommandIntegrationTest : CommandIntegrationTest() {
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
        assertSucceedsWithExpectedStdout(
                success,
                """<?xml version="1.0" encoding="UTF-8"?>
<xml>
<tagml><l>test</l></tagml>
</xml>""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithLeadingLayer() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml|+A,+B>[a|A>Romeo [b|B>loves<a] Juliet<b]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "-l", "B", "transcription")
        assertSucceedsWithExpectedStdout(
                success,
                """<?xml version="1.0" encoding="UTF-8"?>
<xml xmlns:th="http://www.blackmesatech.com/2017/nss/trojan-horse" th:doc="A _default">
<tagml><a th:doc="A" th:sId="a0"/>Romeo <b>loves<a th:doc="A" th:eId="a0"/> Juliet</b></tagml>
</xml>""")
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
        assertSucceedsWithExpectedStdout(
                success,
                """<?xml version="1.0" encoding="UTF-8"?>
<xml>
<l>test</l>
</xml>""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandInView2() {
        runInitCommand()
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml|+A,+B>[phr|A>Cookie Monster [r>really [phr|B>likes<phr|A] cookies<phr|B]<r]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        val viewName = "A"
        val viewFilename = createViewFileName(viewName)
        val viewPath = createFile(viewFilename, "{\"includeLayers\":[\"A\"]}")
        runAddCommand(tagPath, viewPath)
        runCommitAllCommand()
        runCheckoutCommand(viewName)
        val success = cli!!.run(command, "transcription")
        assertSucceedsWithExpectedStdout(
                success,
                """<?xml version="1.0" encoding="UTF-8"?>
<xml xmlns:th="http://www.blackmesatech.com/2017/nss/trojan-horse" th:doc="A">
<tagml><phr th:doc="A" th:sId="phr0"/>Cookie Monster <r>really likes<phr th:doc="A" th:eId="phr0"/> cookies</r></tagml>
</xml>""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       export-xml [-l <leading_layer>] [-o <file>] [-h] <document>

Export the document as xml.

positional arguments:
  <document>             The name of the document to export.

named arguments:
  -l <leading_layer>, --leadinglayer <leading_layer>
                         In case  of  overlapping  layers,  the  layer that
                         defines the xml hierarchy.
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
        private val command = ExportXmlCommand().name
    }
}
