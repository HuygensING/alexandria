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
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext
import nl.knaw.huygens.alexandria.dropwizard.cli.DocumentInfo
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.view.TAGView
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

class StatusCommand : AlexandriaCommand("status", "Show the directory status (active view, modified files, etc.).") {
    override fun configure(subparser: Subparser) {}
    @Throws(IOException::class)
    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val context = readContext()
        System.out.printf("Active view: %s%n%n", context.activeView)
        tagStore.use { store ->
            showDocuments(store, context)
            showViews(store, context)
        }
        println()
        showChanges(context)
    }

    @Throws(IOException::class)
    private fun showChanges(context: CLIContext) {
        val fileStatusMap = readWorkDirStatus(context)
        showChanges(fileStatusMap)
    }

    private fun showDocuments(store: TAGStore, context: CLIContext) {
        val documents = context.documentInfo.keys.stream()
                .sorted()
                .map { docName: String -> docInfo(docName, context.documentInfo[docName]!!, store) }
                .collect(Collectors.joining("\n  "))
        if (documents.isEmpty()) {
            println("no documents")
        } else {
            System.out.printf("Documents:%n  %s%n%n", documents)
        }
    }

    private fun docInfo(
            docName: String,
            documentInfo: DocumentInfo,
            store: TAGStore
    ): String {
        val docId = documentInfo.dbId
        val sourceFile = documentInfo.sourceFile
        return store.runInTransaction<String> {
            val document = store.getDocument(docId)
            String.format(
                    "%s%n    created:  %s%n    modified: %s%n    source: %s",
                    docName, document.creationDate, document.modificationDate, sourceFile)
        }
    }

    private fun showViews(store: TAGStore, context: CLIContext) {
        val views = readViewMap(store, context).entries.stream()
                .map { entry: Map.Entry<String, TAGView> -> this.toString(entry) }
                .collect(Collectors.joining("\n  "))
        if (views.isEmpty()) {
            println("no views")
        } else {
            System.out.printf("Views:%n  %s%n%n", views)
        }
    }

    private fun toString(entry: Map.Entry<String, TAGView>): String {
        val k = entry.key
        val v = entry.value
        val info: MutableList<String> = ArrayList()
        var markupRelevance = ""
        val relevantMarkup: MutableSet<String> = TreeSet()
        if (v.markupStyleIsInclude()) {
            markupRelevance = "included"
            relevantMarkup.addAll(v.markupToInclude)
        } else if (v.markupStyleIsExclude()) {
            markupRelevance = "excluded"
            relevantMarkup.addAll(v.markupToExclude)
        }
        if (relevantMarkup.isNotEmpty()) {
            val markup = relevantMarkup.stream().sorted().collect(Collectors.joining(" "))
            val markupInfo = String.format("%s markup = %s", markupRelevance, markup)
            info.add(markupInfo)
        }
        var layerRelevance = ""
        val relevantLayers: MutableSet<String> = TreeSet()
        if (v.layerStyleIsInclude()) {
            layerRelevance = "included"
            relevantLayers.addAll(v.layersToInclude)
        } else if (v.layerStyleIsExclude()) {
            layerRelevance = "excluded"
            relevantLayers.addAll(v.layersToExclude)
        }
        if (relevantLayers.isNotEmpty()) {
            val layers = relevantLayers.stream().sorted().collect(Collectors.joining(" "))
            val layerInfo = String.format("%s layers = %s", layerRelevance, layers)
            info.add(layerInfo)
        }
        return String.format("%s:\n    %s", k, java.lang.String.join("\n    ", info))
    }
}
