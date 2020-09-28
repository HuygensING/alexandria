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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.google.common.base.Charsets
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import io.dropwizard.cli.Cli
import io.dropwizard.cli.Command
import net.sourceforge.argparse4j.inf.Namespace
import nl.knaw.huc.di.tag.TAGViews.getShowAllMarkupView
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext
import nl.knaw.huygens.alexandria.dropwizard.cli.FileInfo
import nl.knaw.huygens.alexandria.dropwizard.cli.FileType
import nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties
import nl.knaw.huygens.alexandria.storage.BDBTAGStore
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.view.TAGView
import nl.knaw.huygens.alexandria.view.TAGViewDefinition
import nl.knaw.huygens.alexandria.view.TAGViewFactory
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.tuple.Pair
import org.fusesource.jansi.Ansi
import org.fusesource.jansi.AnsiConsole
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.BiPredicate
import java.util.function.Consumer
import java.util.stream.Collectors
import java.util.stream.Collectors.toMap

abstract class AlexandriaCommand(name: String, description: String) : Command(name, description) {
    val FILE = "file"
    var alexandriaDir: String? = null
    private var contextFile: File? = null
    var workDir: String? = null

    fun initPaths(workPath: Path) {
        workDir = System.getProperty(AlexandriaProperties.WORKDIR, workPath.toString())
        alexandriaDir = "$workDir/$ALEXANDRIA_DIR"
        contextFile = File(alexandriaDir, "context.json")
    }

    protected val workingDirectory: Optional<Path>
        get() = getWorkingDirectory(Paths.get("").toAbsolutePath())

    private fun getWorkingDirectory(path: Path?): Optional<Path> {
        if (path == null) return Optional.empty()
        val alexandriaDir = path.resolve(ALEXANDRIA_DIR)
        return if (alexandriaDir.toFile().exists()) {
            Optional.ofNullable(path)
        } else {
            getWorkingDirectory(path.parent)
        }
    }

    fun readViewMap(store: TAGStore, context: CLIContext): MutableMap<String, TAGView> {
        val viewFactory = TAGViewFactory(store)
        return context.tagViewDefinitions.entries.stream()
                .collect(toMap(
                        { e: Map.Entry<String, TAGViewDefinition> -> e.key },
                        { e: Map.Entry<String, TAGViewDefinition> -> viewFactory.fromDefinition(e.value) }
                ))
    }

    fun storeViewMap(viewMap: Map<String, TAGView>, context: CLIContext) {
        val viewDefinitionMap = viewMap.entries.stream()
                .collect(toMap(
                        { e: Map.Entry<String, TAGView> -> e.key },
                        { e: Map.Entry<String, TAGView> -> e.value.definition }
                ))
        context.tagViewDefinitions = viewDefinitionMap
    }

    fun readContext(): CLIContext =
            uncheckedRead(contextFile, CLIContext::class.java)

    fun storeContext(context: CLIContext) {
        uncheckedStore(contextFile, context)
    }

    fun checkAlexandriaIsInitialized() {
        if (!contextFile!!.exists()) {
            println(
                    "This directory (or any of its parents) has not been initialized for alexandria, run ")
            println("  alexandria init")
            println("first. (In this, or a parent directory)")
            throw AlexandriaCommandException("not initialized")
        }
    }

    private fun uncheckedStore(file: File?, `object`: Any) {
        try {
            mapper.writeValue(file, `object`)
        } catch (e: IOException) {
            throw UncheckedIOException(e)
        }
    }

    private fun <T> uncheckedRead(file: File?, clazz: Class<T>): T =
            try {
                mapper.readValue(file, clazz)
            } catch (e: IOException) {
                throw UncheckedIOException(e)
            }

    fun getIdForExistingDocument(docName: String): Long {
        val context = readContext()
        val documentIndex = context.documentInfo
        if (!documentIndex.containsKey(docName)) {
            val registeredDocuments = context.documentInfo.keys.stream()
                    .sorted()
                    .map { d: String? -> "  $d" }
                    .collect(Collectors.joining(System.lineSeparator()))
            val message = String.format(
                    "ERROR: No document '%s' was registered.\nRegistered documents:%n%s",
                    docName, registeredDocuments)
            throw AlexandriaCommandException(message)
        }
        return documentIndex[docName]!!.dbId!!
    }

    fun getExistingView(viewName: String, store: TAGStore, context: CLIContext): TAGView {
        val viewMap = readViewMap(store, context)
        if (!viewMap.containsKey(viewName)) {
            val registeredViews = viewMap.keys.stream().sorted().map { v: String -> "  $v" }.collect(Collectors.joining(System.lineSeparator()))
            val message = String.format(
                    "ERROR: No view '%s' was registered.\nRegistered views:%n%s",
                    viewName, registeredViews)
            throw AlexandriaCommandException(message)
        }
        return viewMap[viewName]!!
    }

    fun workFilePath(relativePath: String): Path =
            Paths.get(workDir).toAbsolutePath().resolve(relativePath)

    fun pathRelativeToWorkDir(relativePath: String?): Path {
        val other = Paths.get("").resolve(relativePath).toAbsolutePath()
        return relativeToWorkDir(Paths.get(workDir).toAbsolutePath(), other)
    }

    val tagStore: TAGStore
        get() = BDBTAGStore(alexandriaDir, false)

    fun fileType(fileName: String): FileType {
        if (fileName.endsWith(".tagml") || fileName.endsWith(".tag")) {
            return FileType.TAGML_SOURCE
        }
        return if (fileName.endsWith(".json")) {
            FileType.VIEW_DEFINITION
        } else FileType.OTHER
    }

    override fun onError(cli: Cli, namespace: Namespace, e: Throwable) {
        cli.stdErr.println(e.message)
    }

    fun exportTAGML(
            context: CLIContext,
            store: TAGStore,
            tagView: TAGView,
            fileName: String,
            docId: Long) {
        val document = store.getDocument(docId)
        val tagmlExporter = TAGMLExporter(store, tagView)
        val tagml = tagmlExporter.asTAGML(document).replace("\n\\s*\n".toRegex(), "\n").trim { it <= ' ' }
        try {
            val out = workFilePath(fileName).toFile()
            FileUtils.writeStringToFile(out, tagml, Charsets.UTF_8)
            context.watchedFiles[fileName]!!.withLastCommit(Instant.now())
        } catch (e: IOException) {
            e.printStackTrace()
            throw UncheckedIOException(e)
        }
    }

    fun checkoutView(viewName: String) {
        val showAll = MAIN_VIEW == viewName
        if (showAll) {
            println("Checking out main view...")
        } else {
            System.out.printf("Checking out view %s...%n", viewName)
        }
        tagStore.use { store ->
            val context = readContext()
            val watchedTranscriptions: MutableMap<String?, FileInfo?> = HashMap()
            context.watchedFiles.entries.stream()
                    .filter { e: Map.Entry<String, FileInfo> -> e.value.fileType == FileType.TAGML_SOURCE }
                    .forEach { e: Map.Entry<String, FileInfo> ->
                        val fileName = e.key
                        val fileInfo = e.value
                        watchedTranscriptions[fileName] = fileInfo
                    }
            val documentIndex = context.documentInfo
            store.runInTransaction {
                val tagView = if (showAll) getShowAllMarkupView(store) else getExistingView(viewName, store, context)
                watchedTranscriptions.keys.stream()
                        .sorted()
                        .forEach { fileName: String? ->
                            val fileInfo = watchedTranscriptions[fileName]!!
                            System.out.printf("  updating %s...%n", fileName)
                            val documentName = fileInfo.objectName!!
                            val docId = documentIndex[documentName]?.dbId!!
                            exportTAGML(context, store, tagView, fileName!!, docId)
                            try {
                                val lastModified = Files.getLastModifiedTime(workFilePath(fileName)).toInstant()
                                context.watchedFiles[fileName]?.withLastCommit(lastModified)
                            } catch (e: IOException) {
                                throw RuntimeException(e)
                            }
                        }
            }
            context.activeView = viewName
            storeContext(context)
        }
    }

    protected fun toViewName(fileName: String): String =
            fileName.replace("^.*$VIEWS_DIR${"/".toRegex()}", "")
                    .replace(".json".toRegex(), "")

    @Throws(IOException::class)
    protected fun makeFileInfo(filePath: Path): FileInfo {
        val lastModifiedInstant = Files.getLastModifiedTime(filePath).toInstant()
        val lastCommit = lastModifiedInstant.minus(
                365L, ChronoUnit.DAYS) // set lastCommit to instant sooner than lastModifiedInstant
        return FileInfo().withLastCommit(lastCommit)
    }

    enum class FileStatus {
        // all status is since last commit
        CHANGED,   // changed, can be committed
        UNCHANGED, // not changed
        DELETED,   // deleted, committing will remove the document
        CREATED    // needs to be added first
    }

    @Throws(IOException::class)
    fun readWorkDirStatus(context: CLIContext): Multimap<FileStatus, String> {
        val fileStatusMap: Multimap<FileStatus, String> = ArrayListMultimap.create()
        val workDir = workFilePath("")
        addNewFilesFromWatchedDirs(context)
        val watchedFiles: MutableSet<String?> = HashSet(context.watchedFiles.keys)
        val matcher0 = BiPredicate { filePath: Path, fileAttr: BasicFileAttributes ->
            (isRelevantFile(workDir, filePath, fileAttr)
                    || fileAttr.isDirectory && isNotDotDirectory(filePath))
        }
        val matcher = BiPredicate { filePath: Path, fileAttr: BasicFileAttributes ->
            (isRelevantFile(workDir, filePath, fileAttr)
                    || isRelevantDirectory(filePath, fileAttr, matcher0))
        }
        for (p in context.watchedDirectories) {
            val absolutePath = Paths.get(this.workDir).resolve(p)
            Files.find(absolutePath, 1, matcher)
                    .forEach { path: Path -> putFileStatus(workDir, path, fileStatusMap, context, watchedFiles) }
        }
        watchedFiles.forEach(Consumer { f: String? -> fileStatusMap.put(FileStatus.DELETED, f) })
        return fileStatusMap
    }

    private fun addNewFilesFromWatchedDirs(context: CLIContext) {
        val watchedFiles = context.watchedFiles
        val workDirPath = workFilePath("")
        context.watchedDirectories.stream()
                .map { relativePath: String -> workFilePath(relativePath) }
                .map { obj: Path -> obj.toFile() }
                .map { obj: File -> obj.listFiles() }
                .flatMap { array: Array<File> -> Arrays.stream(array) }
                .filter { obj: File -> obj.isFile }
                .map { obj: File -> obj.toPath() } //        .peek(p -> System.out.println(p))
                .map { p: Path -> Pair.of(p, relativeToWorkDir(workDirPath, p).toString().replace("\\\\".toRegex(), "/")) } //        .peek(p -> System.out.println(p))
                .filter { p: Pair<Path, String> -> !watchedFiles.containsKey(p.right) } //        .peek(p -> System.out.println(p))
                .forEach { p: Pair<Path, String> ->
                    try {
                        watchedFiles[p.right] = makeFileInfo(p.left)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
        storeContext(context)
    }

    private fun isRelevantFile(
            workDir: Path,
            filePath: Path,
            fileAttr: BasicFileAttributes
    ): Boolean =
            (fileAttr.isRegularFile
                    && (isTagmlFile(workDir, filePath) || isViewDefinition(workDir, filePath)))

    private fun isRelevantDirectory(
            filePath: Path,
            fileAttr: BasicFileAttributes,
            matcher: BiPredicate<Path, BasicFileAttributes>): Boolean =
            try {
                (fileAttr.isDirectory
                        && isNotDotDirectory(filePath)
                        && Files.find(filePath, 1, matcher).count() > 1)
            } catch (e: IOException) {
                false
            }

    private fun isNotDotDirectory(filePath: Path): Boolean =
            !filePath.fileName.toString().startsWith(".")

    private fun isTagmlFile(workDir: Path, filePath: Path): Boolean =
            fileType(filePath.toString()) == FileType.TAGML_SOURCE

    private fun relativeToWorkDir(workDirPath: Path, filePath: Path): Path =
            workDirPath.relativize(filePath).normalize()

    private fun isViewDefinition(workDir: Path, filePath: Path): Boolean =
            fileType(filePath.toString()) == FileType.VIEW_DEFINITION

    private fun putFileStatus(
            workDir: Path,
            filePath: Path,
            fileStatusMap: Multimap<FileStatus, String>,
            context: CLIContext,
            watchedFiles: MutableSet<String?>) {
        val file = relativeToWorkDir(workDir, filePath).toString().replace("\\", "/")
        if (watchedFiles.contains(file)) {
            val lastCommit = context.watchedFiles[file]?.lastCommit
            try {
                val lastModified = Files.getLastModifiedTime(filePath).toInstant()
                val fileStatus = if (lastModified.isAfter(lastCommit)) FileStatus.CHANGED else FileStatus.UNCHANGED
                fileStatusMap.put(fileStatus, file)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            watchedFiles.remove(file)
        } else if (!context.watchedDirectories.contains(file)) {
            fileStatusMap.put(FileStatus.CREATED, file)
        }
    }

    fun showChanges(fileStatusMap: Multimap<FileStatus, String>) {
        AnsiConsole.systemInstall()
        val changedFiles: Set<String> = HashSet(fileStatusMap[FileStatus.CHANGED])
        val deletedFiles: Set<String> = HashSet(fileStatusMap[FileStatus.DELETED])
        val currentPath = Paths.get("").toAbsolutePath()
        val workdirPath = Paths.get(workDir)
        if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
            System.out.printf(
                    "Uncommitted changes:%n"
                            + "  (use \"alexandria commit <file>...\" to commit the selected changes)%n"
                            + "  (use \"alexandria commit -a\" to commit all changes)%n"
                            + "  (use \"alexandria revert <file>...\" to discard changes)%n%n")
            val changedOrDeletedFiles: MutableSet<String> = TreeSet()
            changedOrDeletedFiles.addAll(changedFiles)
            changedOrDeletedFiles.addAll(deletedFiles)
            changedOrDeletedFiles.forEach { file: String ->
                val status = if (changedFiles.contains(file)) "        modified: " else "        deleted:  "
                val filePath = workdirPath.resolve(file)
                val relativeToCurrentPath = relativeToWorkDir(currentPath, filePath)
                println(Ansi.ansi().fg(Ansi.Color.RED).a(status).a(relativeToCurrentPath).reset())
            }
        }
        println()
        val createdFiles = fileStatusMap[FileStatus.CREATED]
        if (!createdFiles.isEmpty()) {
            System.out.printf("Untracked files:%n"
                    + "  (use \"alexandria add <file>...\" to start tracking this file.)%n%n")
            createdFiles.stream()
                    .distinct()
                    .sorted()
                    .forEach { file: String ->
                        val filePath = workdirPath.resolve(file)
                        val relativeToCurrentPath = relativeToWorkDir(currentPath, filePath)
                        println(Ansi.ansi().fg(Ansi.Color.RED).a("        ").a(relativeToCurrentPath).reset())
                    }
        }
        AnsiConsole.systemUninstall()
    }

    fun relativeFilePaths(namespace: Namespace): List<String> {
        return namespace.getList<Any>(ARG_FILE).stream()
                .map { obj: Any? -> String::class.java.cast(obj) }
                .map { pathString: String -> this.relativeToWorkDir(pathString) }
                .collect(Collectors.toList())
    }

    private fun relativeToWorkDir(pathString: String): String {
        val path = Paths.get(pathString)
        val absolutePath = if (path.isAbsolute) path else Paths.get("").toAbsolutePath().resolve(pathString)
        return relativeToWorkDir(Paths.get(workDir!!).toAbsolutePath(), absolutePath)
                .toString()
                .replace("\\\\".toRegex(), "/")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(AlexandriaCommand::class.java)
        const val MAIN_VIEW = "-"
        const val SOURCE_DIR = "tagml"
        const val VIEWS_DIR = "views"
        const val SPARQL_DIR = "sparql"
        const val ARG_FILE = "file"
        const val DOCUMENT = "document"
        const val OUTPUTFILE = "outputfile"
        const val ALEXANDRIA_DIR = ".alexandria"
        var mapper: ObjectMapper = ObjectMapper().registerModule(Jdk8Module()).registerModule(JavaTimeModule())
    }

    init {
        val workPath = workingDirectory.orElse(Paths.get("").toAbsolutePath())
        initPaths(workPath)
    }
}
