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
import nl.knaw.huc.di.tag.TAGViews.getShowAllMarkupView
import nl.knaw.huc.di.tag.tagml.TAGML.DEFAULT_LAYER
import nl.knaw.huc.di.tag.tagml.xml.exporter.XMLExporter
import java.io.IOException
import java.nio.file.Files

class ExportXmlCommand : AlexandriaCommand("export-xml", "Export the document as xml.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("document")
                .dest(DOCUMENT)
                .metavar("<document>")
                .type(String::class.java)
                .required(true)
                .help("The name of the document to export.")
        subparser
                .addArgument("-l", "--leadinglayer")
                .dest(LEADING_LAYER)
                .metavar("<leading_layer>")
                .type(String::class.java)
                .required(false)
                .help("In case of overlapping layers, the layer that defines the xml hierarchy.")
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
        val outputFile = namespace.getString(OUTPUTFILE)
        val leadingLayer = namespace.getString(LEADING_LAYER)
        val docId = getIdForExistingDocument(docName)
        tagStore.use { store ->
            store.runInTransaction {
                val context = readContext()
                val viewName = context.activeView
                val tagView = if (MAIN_VIEW == viewName) getShowAllMarkupView(store) else getExistingView(viewName, store, context)
                val document = store.getDocument(docId)
                val xmlExporter = XMLExporter(store, tagView)
                val leading = leadingLayer ?: DEFAULT_LAYER
                val xml = xmlExporter.asXML(document, leading)
                if (outputFile != null) {
                    writeToFile(outputFile, xml)
                } else {
                    println(xml)
                }
            }
        }
    }

    private fun writeToFile(file: String, contents: String) {
        try {
            System.out.printf("exporting to %s...", file)
            Files.write(workFilePath(file), contents.toByteArray())
            println()
            print("done!")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        const val LEADING_LAYER = "leading_layer"
    }
}
