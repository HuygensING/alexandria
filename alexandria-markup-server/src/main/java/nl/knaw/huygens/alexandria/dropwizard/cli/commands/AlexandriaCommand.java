package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Charsets;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.Command;
import net.sourceforge.argparse4j.inf.Namespace;
import nl.knaw.huc.di.tag.TAGViews;
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter;
import nl.knaw.huygens.alexandria.dropwizard.cli.*;
import nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties;
import nl.knaw.huygens.alexandria.storage.BDBTAGStore;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import nl.knaw.huygens.alexandria.view.TAGViewDefinition;
import nl.knaw.huygens.alexandria.view.TAGViewFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.fusesource.jansi.AnsiConsole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.function.BiPredicate;

import static java.lang.System.lineSeparator;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.*;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public abstract class AlexandriaCommand extends Command {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaCommand.class);

  public static final String MAIN_VIEW = "-";

  public static final String SOURCE_DIR = "tagml";
  public static final String VIEWS_DIR = "views";
  public static final String SPARQL_DIR = "sparql";

  public static final String ARG_FILE = "file";

  static final String DOCUMENT = "document";
  static final String OUTPUTFILE = "outputfile";

  static final String ALEXANDRIA_DIR = ".alexandria";
  final String FILE = "file";

  String alexandriaDir;
  private File contextFile;
  String workDir;
  static final ObjectMapper mapper =
      new ObjectMapper().registerModule(new Jdk8Module()).registerModule(new JavaTimeModule());

  public AlexandriaCommand(String name, String description) {
    super(name, description);
    Path workPath = getWorkingDirectory().orElse(Paths.get("").toAbsolutePath());
    initPaths(workPath);
  }

  void initPaths(final Path workPath) {
    workDir = System.getProperty(AlexandriaProperties.WORKDIR, workPath.toString());
    alexandriaDir = workDir + "/" + ALEXANDRIA_DIR;
    contextFile = new File(alexandriaDir, "context.json");
  }

  protected Optional<Path> getWorkingDirectory() {
    return getWorkingDirectory(Paths.get("").toAbsolutePath());
  }

  private Optional<Path> getWorkingDirectory(final Path path) {
    if (path == null) return Optional.empty();
    Path alexandriaDir = path.resolve(ALEXANDRIA_DIR);
    if (alexandriaDir.toFile().exists()) {
      return Optional.ofNullable(path);
    } else {
      return getWorkingDirectory(path.getParent());
    }
  }

  Map<String, TAGView> readViewMap(TAGStore store, final CLIContext context) {
    TAGViewFactory viewFactory = new TAGViewFactory(store);
    return context.getTagViewDefinitions().entrySet().stream()
        .collect(toMap(Map.Entry::getKey, e -> viewFactory.fromDefinition(e.getValue())));
  }

  void storeViewMap(Map<String, TAGView> viewMap, CLIContext context) {
    Map<String, TAGViewDefinition> viewDefinitionMap =
        viewMap.entrySet().stream()
            .collect(toMap(Map.Entry::getKey, e -> e.getValue().getDefinition()));
    context.setTagViewDefinitions(viewDefinitionMap);
  }

  CLIContext readContext() {
    return uncheckedRead(contextFile, CLIContext.class);
  }

  void storeContext(CLIContext context) {
    uncheckedStore(contextFile, context);
  }

  void checkAlexandriaIsInitialized() {
    if (!contextFile.exists()) {
      System.out.println(
          "This directory (or any of its parents) has not been initialized for alexandria, run ");
      System.out.println("  alexandria init");
      System.out.println("first. (In this, or a parent directory)");
      throw new AlexandriaCommandException("not initialized");
    }
  }

  private void uncheckedStore(File file, Object object) {
    try {
      mapper.writeValue(file, object);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private <T> T uncheckedRead(File file, Class<T> clazz) {
    try {
      return mapper.readValue(file, clazz);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  Long getIdForExistingDocument(String docName) {
    CLIContext context = readContext();
    Map<String, DocumentInfo> documentIndex = context.getDocumentInfo();
    if (!documentIndex.containsKey(docName)) {
      final String registeredDocuments =
          context.getDocumentInfo().keySet().stream()
              .sorted()
              .map(d -> "  " + d)
              .collect(joining(lineSeparator()));
      String message =
          String.format(
              "ERROR: No document '%s' was registered.\nRegistered documents:%n%s",
              docName, registeredDocuments);
      throw new AlexandriaCommandException(message);
    }
    return documentIndex.get(docName).getDbId();
  }

  TAGView getExistingView(String viewName, final TAGStore store, final CLIContext context) {
    Map<String, TAGView> viewMap = readViewMap(store, context);
    if (!viewMap.containsKey(viewName)) {
      final String registeredViews =
          viewMap.keySet().stream().sorted().map(v -> "  " + v).collect(joining(lineSeparator()));
      String message =
          String.format(
              "ERROR: No view '%s' was registered.\nRegistered views:%n%s",
              viewName, registeredViews);
      throw new AlexandriaCommandException(message);
    }
    return viewMap.get(viewName);
  }

  Path workFilePath(final String relativePath) {
    return Paths.get(workDir).toAbsolutePath().resolve(relativePath);
  }

  Path pathRelativeToWorkDir(final String relativePath) {
    Path other = Paths.get("").resolve(relativePath).toAbsolutePath();
    return relativeToWorkDir(Paths.get(workDir).toAbsolutePath(), other);
  }

  TAGStore getTAGStore() {
    return new BDBTAGStore(alexandriaDir, false);
  }

  FileType fileType(String fileName) {
    if (fileName.endsWith(".tagml") || fileName.endsWith(".tag")) {
      return FileType.tagmlSource;
    }
    if (fileName.endsWith(".json")) {
      return FileType.viewDefinition;
    }
    return FileType.other;
  }

  @Override
  public void onError(Cli cli, Namespace namespace, Throwable e) {
    cli.getStdErr().println(e.getMessage());
  }

  void exportTAGML(
      final CLIContext context,
      final TAGStore store,
      final TAGView tagView,
      final String fileName,
      final Long docId) {
    TAGDocument document = store.getDocument(docId);
    TAGMLExporter tagmlExporter = new TAGMLExporter(store, tagView);
    String tagml = tagmlExporter.asTAGML(document).replaceAll("\n\\s*\n", "\n").trim();
    try {
      final File out = workFilePath(fileName).toFile();
      FileUtils.writeStringToFile(out, tagml, Charsets.UTF_8);
      context.getWatchedFiles().get(fileName).setLastCommit(Instant.now());
    } catch (IOException e) {
      e.printStackTrace();
      throw new UncheckedIOException(e);
    }
  }

  void checkoutView(final String viewName) {
    boolean showAll = MAIN_VIEW.equals(viewName);

    if (showAll) {
      System.out.println("Checking out main view...");
    } else {
      System.out.printf("Checking out view %s...%n", viewName);
    }
    try (TAGStore store = getTAGStore()) {
      CLIContext context = readContext();
      Map<String, FileInfo> watchedTranscriptions = new HashMap<>();
      context.getWatchedFiles().entrySet().stream()
          .filter(e -> e.getValue().getFileType().equals(FileType.tagmlSource))
          .forEach(
              e -> {
                String fileName = e.getKey();
                FileInfo fileInfo = e.getValue();
                watchedTranscriptions.put(fileName, fileInfo);
              });

      Map<String, DocumentInfo> documentIndex = context.getDocumentInfo();
      store.runInTransaction(
          () -> {
            TAGView tagView =
                showAll
                    ? TAGViews.getShowAllMarkupView(store)
                    : getExistingView(viewName, store, context);

            watchedTranscriptions.keySet().stream()
                .sorted()
                .forEach(
                    fileName -> {
                      FileInfo fileInfo = watchedTranscriptions.get(fileName);
                      System.out.printf("  updating %s...%n", fileName);
                      String documentName = fileInfo.getObjectName();
                      final Long docId = documentIndex.get(documentName).getDbId();
                      exportTAGML(context, store, tagView, fileName, docId);
                      try {
                        Instant lastModified =
                            Files.getLastModifiedTime(workFilePath(fileName)).toInstant();
                        context.getWatchedFiles().get(fileName).setLastCommit(lastModified);
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    });
          });
      context.setActiveView(viewName);
      storeContext(context);
    }
  }

  protected String toViewName(String fileName) {
    return fileName.replaceAll("^.*" + VIEWS_DIR + "/", "").replaceAll(".json", "");
  }

  protected FileInfo makeFileInfo(final Path filePath) throws IOException {
    Instant lastModifiedInstant = Files.getLastModifiedTime(filePath).toInstant();
    Instant lastCommit =
        lastModifiedInstant.minus(
            365L, DAYS); // set lastCommit to instant sooner than lastModifiedInstant
    FileType fileType = fileType(filePath.getFileName().toString());
    return new FileInfo()
        .setFileType(fileType)
        .setObjectName("")
        .setLastCommit(lastCommit);
  }

  enum FileStatus {
    // all status is since last commit
    changed, // changed, can be committed
    unchanged, // not changed
    deleted, // deleted, committing will remove the document
    created, // needs to be added first
  }

  Multimap<FileStatus, String> readWorkDirStatus(CLIContext context) throws IOException {
    Multimap<FileStatus, String> fileStatusMap = ArrayListMultimap.create();
    Path workDir = workFilePath("");
    addNewFilesFromWatchedDirs(context);
    Set<String> watchedFiles = new HashSet<>(context.getWatchedFiles().keySet());
    BiPredicate<Path, BasicFileAttributes> matcher0 =
        (filePath, fileAttr) ->
            isRelevantFile(workDir, filePath, fileAttr)
                || (fileAttr.isDirectory() && isNotDotDirectory(filePath));
    BiPredicate<Path, BasicFileAttributes> matcher =
        (filePath, fileAttr) ->
            isRelevantFile(workDir, filePath, fileAttr)
                || isRelevantDirectory(filePath, fileAttr, matcher0);

    for (String p : context.getWatchedDirectories()) {
      Path absolutePath = Paths.get(this.workDir).resolve(p);
      Files.find(absolutePath, 1, matcher)
          .forEach(path -> putFileStatus(workDir, path, fileStatusMap, context, watchedFiles));
    }
    watchedFiles.forEach(f -> {
      Path filePath = Paths.get(this.workDir).resolve(f);
      FileStatus fileStatus = fileStatus(f, context, filePath);
      fileStatusMap.put(fileStatus, f);
    });
    return fileStatusMap;
  }

  FileStatus fileStatus(String file, CLIContext context, Path filePath){
    Instant lastCommit = context.getWatchedFiles().get(file).getLastCommit();
    try {
      Instant lastModified = Files.getLastModifiedTime(filePath).toInstant();
      return lastModified.isAfter(lastCommit) ? FileStatus.changed : FileStatus.unchanged;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void addNewFilesFromWatchedDirs(final CLIContext context) {
    final Map<String, FileInfo> watchedFiles = context.getWatchedFiles();
    Path workDirPath = workFilePath("");
    context.getWatchedDirectories().stream()
        .map(this::workFilePath)
        .map(Path::toFile)
        .map(File::listFiles)
        .flatMap(Arrays::stream)
        .filter(File::isFile)
        .map(File::toPath)
        //        .peek(p -> System.out.println(p))
        .map(p -> Pair.of(p, relativeToWorkDir(workDirPath, p).toString().replaceAll("\\\\", "/")))
        //        .peek(p -> System.out.println(p))
        .filter(p -> !watchedFiles.containsKey(p.getRight()))
        //        .peek(p -> System.out.println(p))
        .forEach(
            p -> {
              try {
                watchedFiles.put(p.getRight(), makeFileInfo(p.getLeft()));
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
    storeContext(context);
  }

  private boolean isRelevantFile(
      final Path workDir, final Path filePath, final BasicFileAttributes fileAttr) {
    return fileAttr.isRegularFile()
        && (isTagmlFile(workDir, filePath) || isViewDefinition(workDir, filePath));
  }

  private boolean isRelevantDirectory(
      final Path filePath,
      final BasicFileAttributes fileAttr,
      final BiPredicate<Path, BasicFileAttributes> matcher) {
    try {
      return fileAttr.isDirectory()
          && isNotDotDirectory(filePath)
          && Files.find(filePath, 1, matcher).count() > 1;
    } catch (IOException e) {
      return false;
    }
  }

  private boolean isNotDotDirectory(final Path filePath) {
    return !filePath.getFileName().toString().startsWith(".");
  }

  private boolean isTagmlFile(final Path workDir, final Path filePath) {
    return /*relativeToWorkDir(workDir, filePath).startsWith(Paths.get(SOURCE_DIR))
           && */ fileType(filePath.toString()).equals(FileType.tagmlSource);
  }

  private Path relativeToWorkDir(final Path workDirPath, final Path filePath) {
    return workDirPath.relativize(filePath).normalize();
  }

  private boolean isViewDefinition(final Path workDir, final Path filePath) {
    return /*relativeToWorkDir(workDir, filePath).startsWith(Paths.get(VIEWS_DIR))
           && */ fileType(filePath.toString()).equals(FileType.viewDefinition);
  }

  private void putFileStatus(
      Path workDir,
      Path filePath,
      Multimap<FileStatus, String> fileStatusMap,
      CLIContext context,
      Set<String> watchedFiles
  ) {
    String file = relativeToWorkDir(workDir, filePath).toString().replace("\\", "/");
    if (watchedFiles.contains(file)) {
      Instant lastCommit = context.getWatchedFiles().get(file).getLastCommit();
      try {
        Instant lastModified = Files.getLastModifiedTime(filePath).toInstant();
        FileStatus fileStatus =
            lastModified.isAfter(lastCommit) ? FileStatus.changed : FileStatus.unchanged;
        fileStatusMap.put(fileStatus, file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      watchedFiles.remove(file);
    } else if (!context.getWatchedDirectories().contains(file)) {
      fileStatusMap.put(FileStatus.created, file);
    }
  }

  void showChanges(Multimap<FileStatus, String> fileStatusMap) {
    AnsiConsole.systemInstall();

    Set<String> changedFiles = new HashSet<>(fileStatusMap.get(FileStatus.changed));
    Set<String> deletedFiles = new HashSet<>(fileStatusMap.get(FileStatus.deleted));
    Path currentPath = Paths.get("").toAbsolutePath();
    Path workdirPath = Paths.get(workDir);
    if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
      System.out.printf(
          "Uncommitted changes:%n"
              + "  (use \"alexandria commit <file>...\" to commit the selected changes)%n"
              + "  (use \"alexandria commit -a\" to commit all changes)%n"
              + "  (use \"alexandria revert <file>...\" to discard changes)%n%n");
      Set<String> changedOrDeletedFiles = new TreeSet<>();
      changedOrDeletedFiles.addAll(changedFiles);
      changedOrDeletedFiles.addAll(deletedFiles);
      changedOrDeletedFiles.forEach(
          file -> {
            String status =
                changedFiles.contains(file) ? "        modified: " : "        deleted:  ";
            Path filePath = workdirPath.resolve(file);
            Path relativeToCurrentPath = relativeToWorkDir(currentPath, filePath);
            System.out.println(ansi().fg(RED).a(status).a(relativeToCurrentPath).reset());
          });
    }

    System.out.println();

    Collection<String> createdFiles = fileStatusMap.get(FileStatus.created);
    if (!createdFiles.isEmpty()) {
      System.out.printf(
          "Untracked files:%n"
              + "  (use \"alexandria add <file>...\" to start tracking this file.)%n%n");
      createdFiles.stream()
          .distinct()
          .sorted()
          .forEach(
              file -> {
                Path filePath = workdirPath.resolve(file);
                Path relativeToCurrentPath = relativeToWorkDir(currentPath, filePath);
                System.out.println(ansi().fg(RED).a("        ").a(relativeToCurrentPath).reset());
              });
    }

    AnsiConsole.systemUninstall();
  }

  List<String> relativeFilePaths(final Namespace namespace) {
    return namespace.getList(ARG_FILE).stream()
        .map(String.class::cast)
        .map(this::relativeToWorkDir)
        .collect(toList());
  }

  private String relativeToWorkDir(final String pathString) {
    Path path = Paths.get(pathString);
    Path absolutePath =
        path.isAbsolute() ? path : Paths.get("").toAbsolutePath().resolve(pathString);
    return relativeToWorkDir(Paths.get(workDir).toAbsolutePath(), absolutePath)
        .toString()
        .replaceAll("\\\\", "/");
  }
}
