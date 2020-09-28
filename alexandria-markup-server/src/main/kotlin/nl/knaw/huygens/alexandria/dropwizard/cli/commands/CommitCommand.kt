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
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.impl.Arguments
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.dropwizard.api.NamedDocumentService
import nl.knaw.huygens.alexandria.dropwizard.cli.*
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.view.TAGViewFactory
import org.apache.commons.io.FileUtils
import org.jooq.lambda.Unchecked
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer
import java.util.stream.Collectors

class CommitCommand : AlexandriaCommand("commit", "Record changes to the repository.") {
    private val ARG_ALL = "add_all"
    override fun configure(subparser: Subparser) {
        subparser
                .addArgument("-a")
                .dest(ARG_ALL)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .required(false)
                .help("automatically add all changed files")
        subparser
                .addArgument(ARG_FILE)
                .metavar("<file>")
                .dest(FILE)
                .type(String::class.java)
                .nargs("*")
                .required(false)
                .help("the changed file(s)")
        subparser.epilog(
                "Warning: currently, committing tagml changes is only possible in the main view!")
    }

    override fun run(bootstrap: Bootstrap<*>?, namespace: Namespace) {
        checkAlexandriaIsInitialized()
        val argAll = namespace.getBoolean(ARG_ALL)
        val fileNames = if (argAll) modifiedWatchedFileNames else relativeFilePaths(namespace)
        if (fileNames.isEmpty() && !argAll) {
            throw AlexandriaCommandException(
                    "Commit aborted: no files specified. Use -a to commit all changed tracked files.")
        }
        tagStore.use { store ->
            store.runInTransaction {
//                val service = NamedDocumentService(store)
                val context = readContext()
                val activeView = context.activeView
                val inMainView = activeView == MAIN_VIEW
                val tagmlReverts: MutableList<String?> = ArrayList()
                val viewRevert = AtomicReference("")
                fileNames.forEach(
                        Consumer { fileName: String? ->
                            val fileType = fileType(fileName!!)
                            var objectName = fileName
                            var ok = true
                            when (fileType) {
                                FileType.TAGML_SOURCE -> if (context.getDocumentName(fileName).isPresent && !inMainView) {
                                    System.err.println("unable to commit $fileName")
                                    tagmlReverts.add(fileName)
                                    ok = false
                                } else {
                                    objectName = toDocName(fileName)
                                    processTAGMLFile(context, store, fileName, objectName)
                                }
                                FileType.VIEW_DEFINITION -> {
                                    objectName = toViewName(fileName)
                                    if (context.activeView == objectName) {
                                        viewRevert.set(fileName)
                                    } else {
                                        processViewDefinition(store, fileName, objectName, context)
                                    }
                                }
                                FileType.OTHER -> processOtherFile(fileName)
                            }
                            if (ok) {
                                context
                                        .watchedFiles[fileName] = FileInfo()
                                        .withObjectName(objectName)
                                        .withFileType(fileType)
                                        .withLastCommit(Instant.now())
                            }
                        })
                storeContext(context)
                val reverts: MutableList<String?> = ArrayList()
                var errorMsg = ""
                val viewFileName = viewRevert.get()
                if (tagmlReverts.isNotEmpty()) {
                    reverts.addAll(tagmlReverts)
                    errorMsg += String.format(
                            "View %s is active. Currently, committing changes to existing documents is only allowed in the main view.\n",
                            activeView)
                }
                if (viewFileName!!.isNotEmpty()) {
                    reverts.add(viewFileName)
                    errorMsg += String.format(
                            "You are trying to modify the definition file %s of the active view %s. This is not allowed.\n",
                            viewFileName, activeView)
                }
                if (errorMsg.isNotEmpty()) {
                    val revertCommands = reverts.stream().map { r: String? -> "  alexandria revert $r\n" }.collect(Collectors.joining())
                    System.err.printf(
                            "%s%nUse:%n"
                                    + "%s"
                                    + "  alexandria checkout -%n"
                                    + "to undo those changes and return to the main view.%n",
                            errorMsg, revertCommands)
                    throw AlexandriaCommandException("some commits failed")
                }
            }
        }
        println("done!")
    }

    private fun processTAGMLFile(
            context: CLIContext,
            store: TAGStore,
            fileName: String,
            docName: String
    ) {
        System.out.printf("Parsing %s to document %s...%n", fileName, docName)
        store.runInTransaction(
                Unchecked.runnable {
                    val tagmlImporter = TAGMLImporter(store)
                    val file = workFilePath(fileName).toFile()
                    val fileInputStream = FileUtils.openInputStream(file)
                    val document = tagmlImporter.importTAGML(fileInputStream)
                    val service = NamedDocumentService(store)
                    service.registerDocument(document, docName)
                    val newDocumentInfo = DocumentInfo()
                            .withDocumentName(docName)
                            .withSourceFile(fileName)
                            .withDbId(document.dbId)
                    context.documentInfo[docName] = newDocumentInfo
                })
    }

    private fun processViewDefinition(
            store: TAGStore,
            fileName: String,
            viewName: String,
            context: CLIContext
    ) {
        val viewMap = readViewMap(store, context)
        val viewFile = workFilePath(fileName).toFile()
        val viewFactory = TAGViewFactory(store)
        System.out.printf("Parsing %s to view %s...%n", fileName, viewName)
        try {
            val json = FileUtils.readFileToString(viewFile, Charsets.UTF_8)
            val view = viewFactory.fromJsonString(json)
            if (!view.isValid) {
                throw AlexandriaCommandException(String.format(
                        "Commit aborted: Invalid view definition in %s: none of the allowed options %s, %s, %s or %s was found.",
                        fileName,
                        TAGViewFactory.INCLUDE_LAYERS,
                        TAGViewFactory.EXCLUDE_LAYERS,
                        TAGViewFactory.INCLUDE_MARKUP,
                        TAGViewFactory.EXCLUDE_MARKUP))
            }
            viewMap[viewName] = view
            storeViewMap(viewMap, context)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun processOtherFile(fileName: String?) {}

    private fun toDocName(fileName: String): String =
            fileName.replace("^.*$SOURCE_DIR${"/".toRegex()}", "")
                    .replace(".tag(ml)?".toRegex(), "")

    private val modifiedWatchedFileNames: List<String>
        get() {
            val modifiedFiles: MutableList<String> = ArrayList()
            val cliContext = readContext()
            cliContext
                    .watchedFiles
                    .forEach { (k: String, v: FileInfo) ->
                        val filePath = Paths.get(workDir).resolve(k)
                        val lastCommitted = v.lastCommit
                        try {
                            val lastModifiedTime = Files.getLastModifiedTime(filePath)
                            if (lastModifiedTime.toInstant().isAfter(lastCommitted)) {
                                modifiedFiles.add(k)
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
            return modifiedFiles
        }
}
