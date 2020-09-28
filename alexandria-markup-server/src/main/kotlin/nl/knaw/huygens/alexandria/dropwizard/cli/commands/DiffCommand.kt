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
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huc.di.tag.TAGViews.getShowAllMarkupView
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.compare.TAGComparison
import nl.knaw.huygens.alexandria.compare.TAGComparison2
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext
import nl.knaw.huygens.alexandria.storage.TAGStore
import org.apache.commons.io.FileUtils
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.charset.StandardCharsets
import java.util.*

class DiffCommand : AlexandriaCommand("diff", "Show the changes made to the file.") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("-m")
                .dest(ARG_MACHINE_READABLE)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .required(false)
                .help("Output the diff in a machine-readable format")
        subparser
                .addArgument("file")
                .dest(FILE)
                .type(String::class.java)
                .required(true)
                .help("The file containing the edited view")
    }

    override fun run(bootstrap: Bootstrap<*>, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val machineReadable = namespace.getBoolean(ARG_MACHINE_READABLE)
        tagStore.use { store ->
            store.runInTransaction {
                val context = readContext()
                val filename = namespace.getString(FILE)
                val documentName = context.getDocumentName(filename)
                if (documentName.isPresent) {
                    doDiff(store, context, filename, documentName, machineReadable)
                } else {
                    throw AlexandriaCommandException("No document registered for $filename")
                }
            }
        }
    }

    private fun doDiff(
            store: TAGStore,
            context: CLIContext,
            filename: String,
            documentName: Optional<String>,
            machineReadable: Boolean) {
        val documentId = getIdForExistingDocument(documentName.get())
        val original = store.getDocument(documentId)
        val viewName = context.activeView
        val tagView = if (MAIN_VIEW == viewName) getShowAllMarkupView(store) else getExistingView(viewName, store, context)
        val editedFile = workFilePath(filename).toFile()
        try {
            val newTAGML = FileUtils.readFileToString(editedFile, StandardCharsets.UTF_8)
            val importer = TAGMLImporter(store)
            val edited = importer.importTAGML(newTAGML)
            val comparison2 = TAGComparison2(original, tagView, edited, store)
            if (machineReadable) {
                if (comparison2.hasDifferences()) {
                    System.out.printf(
                            "%s%n\t", java.lang.String.join(System.lineSeparator() + "\t", comparison2.mrDiffLines))
                }
            } else {
                val comparison = TAGComparison(original, tagView, edited)
                if (MAIN_VIEW == viewName) {
                    System.out.printf("diff for %s:%n", filename)
                } else {
                    System.out.printf("diff for %s, using view %s:%n", filename, viewName)
                }
                if (comparison.hasDifferences()) {
                    System.out.printf("%s%n", java.lang.String.join(System.lineSeparator(), comparison.getDiffLines()))
                } else {
                    println("no changes")
                }
                //        System.out.printf("%nmarkup diff:%n", filename);
                println("\nmarkup diff:")
                if (comparison2.hasDifferences()) {
                    System.out.printf(
                            "%s%n\t", java.lang.String.join(System.lineSeparator() + "\t", comparison2.diffLines))
                } else {
                    println("no changes")
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            throw UncheckedIOException(e)
        }
    }

    companion object {
        private const val ARG_MACHINE_READABLE = "machine_readable"
    }
}
