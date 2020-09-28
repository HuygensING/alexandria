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

import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huc.di.tag.model.graph.DotFactory
import java.io.IOException

abstract class AbstractGraphvizCommand(name: String, description: String) : AlexandriaCommand(name, description) {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("DOCUMENT")
                .metavar("<document>")
                .dest(DOCUMENT)
                .type(String::class.java)
                .required(true)
                .help("The name of the document to export.")
        subparser
                .addArgument("-o", "--outputfile")
                .dest(OUTPUTFILE)
                .metavar("<file>")
                .type(String::class.java)
                .required(false)
                .help("The file to export to.")
    }

    override fun run(bootstrap: Bootstrap<*>, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val docName = namespace.getString(DOCUMENT)
        val fileName = namespace.getString(OUTPUTFILE)
        val docId = getIdForExistingDocument(docName)
        tagStore.use { store ->
            store.runInTransaction {
                val document = store.getDocument(docId)
                val dotFactory = DotFactory() // TODO: add option to export using view
                val dot = dotFactory.toDot(document, "")
                if (fileName != null) {
                    System.out.printf("exporting to %s...", fileName)
                    try {
                        renderToFile(dot, fileName)
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                    println()
                    println("done!")
                } else {
                    renderToStdOut(dot)
                }
            }
        }
    }

    protected abstract val format: String

    @Throws(IOException::class)
    protected abstract fun renderToFile(dot: String, fileName: String)

    protected abstract fun renderToStdOut(dot: String)
}
