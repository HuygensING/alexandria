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

import com.google.common.base.Charsets
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class ExportDotCommand : AbstractGraphvizCommand("export-dot", "Export the document as .dot file.") {

    override val format: String
        get() = "dot"

    @Throws(IOException::class)
    override fun renderToFile(dot: String, fileName: String) {
        FileUtils.writeStringToFile(File(fileName), dot, Charsets.UTF_8)
    }

    override fun renderToStdOut(dot: String) {
        println(dot)
    }
}
