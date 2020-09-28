package nl.knaw.huygens.alexandria.dropwizard.cli.commands

/*
* #%L
 * alexandria-markup-server
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

import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException
import nl.knaw.huygens.graphviz.DotEngine
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ExportRenderedDotCommand(override val format: String) : AbstractGraphvizCommand(
        "export-$format",
        "Export the document as $format. (Requires access to Graphviz' dot command)") {
    override fun renderToFile(dot: String, fileName: String) {
        val dotEngine = setupDotEngine()
        val file = File(fileName)
        try {
            FileOutputStream(file).use { fos ->
                file.createNewFile()
                dotEngine.renderAs(format, dot, fos)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun renderToStdOut(dot: String) {
        val dotEngine = setupDotEngine()
        dotEngine.renderAs(format, dot, System.out)
    }

    private fun setupDotEngine(): DotEngine {
        val dotEngine = DotEngine()
        val dotVersion = dotEngine.dotVersion
        if (dotVersion.isEmpty()) {
            throw AlexandriaCommandException("""
                This command needs access to the Graphviz dot command, which was not found.
                See https://www.graphviz.org/ for installation instructions.
                """.trimIndent())
        }
        return dotEngine
    }
}
