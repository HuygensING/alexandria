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

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huc.di.tag.TAGViews.getShowAllMarkupView
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext
import nl.knaw.huygens.alexandria.dropwizard.cli.FileType
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.view.TAGView
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.function.Consumer

class RevertCommand : AlexandriaCommand("revert", "Restore the document file(s).") {
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument(ARG_FILE)
                .metavar("<file>")
                .dest(FILE)
                .type(String::class.java)
                .nargs("+")
                .required(true)
                .help("the file to be reverted")
    }

    override fun run(bootstrap: Bootstrap<*>, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val files = relativeFilePaths(namespace)
        val context = readContext()
        val viewName = context.activeView
        val showAll = MAIN_VIEW == viewName
        tagStore.use { store ->
            store.runInTransaction {
                val tagView = if (showAll) getShowAllMarkupView(store) else getExistingView(viewName, store, context)
                files.forEach(
                        Consumer { fileName: String? ->
                            val fileType = fileType(fileName!!)
                            if (fileType == FileType.TAGML_SOURCE) {
                                handleTagmlRevert(context, store, tagView, fileName)
                            } else if (fileType == FileType.VIEW_DEFINITION) {
                                handleViewDefinitionRevert(context, fileName)
                            } // else: it's an uncommittable file.
                        })
                storeContext(context)
            }
        }
        println("done!")
    }

    private fun handleViewDefinitionRevert(context: CLIContext, fileName: String) {
        val viewName = toViewName(fileName)
        val tagViewDefinition = context.tagViewDefinitions[viewName]
        if (tagViewDefinition != null) {
            val objectMapper = ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
            try {
                System.out.printf("Reverting %s...%n", fileName)
                objectMapper.writeValue(File(workDir, fileName), tagViewDefinition)
                updateFileInfo(context, fileName)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        } else {
            System.out.printf("%s is not linked to a view, not reverting.%n", fileName)
        }
    }

    private fun handleTagmlRevert(
            context: CLIContext,
            store: TAGStore,
            tagView: TAGView,
            fileName: String
    ) {
        val documentName = context.getDocumentName(fileName)
        if (documentName.isPresent) {
            System.out.printf("Reverting %s...%n", fileName)
            val docId = getIdForExistingDocument(documentName.get())
            exportTAGML(context, store, tagView, fileName, docId)
            updateFileInfo(context, fileName)
        } else {
            System.out.printf("%s is not linked to a document, not reverting.%n", fileName)
        }
    }

    private fun updateFileInfo(context: CLIContext, fileName: String) {
        val fileInfo = context.watchedFiles[fileName]
        try {
            val file = workFilePath(fileName)
            val lastModifiedTime = Files.getLastModifiedTime(file)
            fileInfo!!.lastCommit = lastModifiedTime.toInstant()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
