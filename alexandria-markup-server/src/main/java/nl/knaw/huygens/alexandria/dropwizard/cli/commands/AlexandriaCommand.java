package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
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
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import nl.knaw.huygens.alexandria.view.TAGViewDefinition;
import nl.knaw.huygens.alexandria.view.TAGViewFactory;
import org.apache.commons.io.FileUtils;
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
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public abstract class AlexandriaCommand extends Command {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaCommand.class);

  public static final String MAIN_VIEW = "-";

  public static final String SOURCE_DIR = "tagml";
  public static final String VIEWS_DIR = "views";
  static final String DOCUMENT = "document";
  static final String OUTPUTFILE = "outputfile";

  static final String ALEXANDRIA_DIR = ".alexandria";
  final String FILE = "file";

  private final String alexandriaDir;
  private final File contextFile;
  final String workDir;
  static ObjectMapper mapper = new ObjectMapper()
      .registerModule(new Jdk8Module())//
      .registerModule(new JavaTimeModule());

  public AlexandriaCommand(String name, String description) {
    super(name, description);
    workDir = System.getProperty(AlexandriaProperties.WORKDIR, ".");
    alexandriaDir = workDir + "/" + ALEXANDRIA_DIR;
    initProjectDir();

    contextFile = new File(alexandriaDir, "context.json");
  }

  private void initProjectDir() {
    new File(alexandriaDir).mkdir();
  }

  Map<String, TAGView> readViewMap(TAGStore store, final CLIContext context) {
    TAGViewFactory viewFactory = new TAGViewFactory(store);
    return context.getTagViewDefinitions()
        .entrySet()
        .stream()
        .collect(toMap(
            Map.Entry::getKey,
            e -> viewFactory.fromDefinition(e.getValue())
        ));
  }

  void storeViewMap(Map<String, TAGView> viewMap, CLIContext context) {
    Map<String, TAGViewDefinition> viewDefinitionMap = viewMap.entrySet()//
        .stream()//
        .collect(toMap(//
            Map.Entry::getKey,//
            e -> e.getValue().getDefinition()//
        ));
    context.setTagViewDefinitions(viewDefinitionMap);
  }

  CLIContext readContext() {
    return uncheckedRead(contextFile, CLIContext.class);
  }

  void storeContext(CLIContext context) {
    uncheckedStore(contextFile, context);
  }

  void checkDirectoryIsInitialized() {
    if (!contextFile.exists()) {
      System.out.println("This directory has not been initialized, run ");
      System.out.println("  alexandria init");
      System.out.println("first.");
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
      final String registeredDocuments = context.getDocumentInfo()
          .keySet()
          .stream()
          .sorted()
          .map(d -> "  " + d)
          .collect(joining(lineSeparator()));
      String message = String.format("ERROR: No document '%s' was registered.\nRegistered documents:%n%s", docName, registeredDocuments);
      throw new AlexandriaCommandException(message);
    }
    return documentIndex.get(docName).getDbId();
  }

  TAGView getExistingView(String viewName, final TAGStore store, final CLIContext context) {
    Map<String, TAGView> viewMap = readViewMap(store, context);
    if (!viewMap.containsKey(viewName)) {
      final String registeredViews = viewMap
          .keySet()
          .stream()
          .sorted()
          .map(v -> "  " + v)
          .collect(joining(lineSeparator()));
      String message = String.format("ERROR: No view '%s' was registered.\nRegistered views:%n%s", viewName, registeredViews);
      throw new AlexandriaCommandException(message);
    }
    return viewMap.get(viewName);
  }

  Path workFilePath(final String relativePath) {
    return Paths.get(workDir).resolve(relativePath);
  }

  TAGStore getTAGStore() {
    return new TAGStore(alexandriaDir, false);
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

  void exportTAGML(final CLIContext context, final TAGStore store, final TAGView tagView, final String fileName, final Long docId) {
    TAGDocument document = store.getDocument(docId);
    TAGMLExporter tagmlExporter = new TAGMLExporter(store, tagView);
    String tagml = tagmlExporter.asTAGML(document)
        .replaceAll("\n\\s*\n", "\n")
        .trim();
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
      context.getWatchedFiles().entrySet()
          .stream()
          .filter(e -> e.getValue().getFileType().equals(FileType.tagmlSource))
          .forEach(e -> {
            String fileName = e.getKey();
            FileInfo fileInfo = e.getValue();
            watchedTranscriptions.put(fileName, fileInfo);
          });

      Map<String, DocumentInfo> documentIndex = context.getDocumentInfo();
      store.runInTransaction(() -> {
        TAGView tagView = showAll
            ? TAGViews.getShowAllMarkupView(store)
            : getExistingView(viewName, store, context);
        watchedTranscriptions.forEach((fileName, fileInfo) -> {
          System.out.printf("  updating %s...%n", fileName);
          String documentName = fileInfo.getObjectName();
          final Long docId = documentIndex.get(documentName).getDbId();
          exportTAGML(context, store, tagView, fileName, docId);
          try {
            Instant lastModified = Files.getLastModifiedTime(workFilePath(fileName)).toInstant();
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

  enum FileStatus {
    // all status is since last commit
    changed, // changed, can be committed
    unchanged, // not changed
    deleted, // deleted, committing will remove the document
    created, // needs to be added first
  }

  Multimap<FileStatus, String> readWorkDirStatus(CLIContext context) throws IOException {
    Multimap<FileStatus, String> fileStatusMap = ArrayListMultimap.create();
    Path workDir = workFilePath(".");
    Set<String> watchedFiles = new HashSet<>(context.getWatchedFiles().keySet());
    BiPredicate<Path, BasicFileAttributes> matcher = (filePath, fileAttr) ->
        fileAttr.isRegularFile()
            && (isTagmlFile(workDir, filePath) || isViewDefinition(workDir, filePath));

    Files.find(workDir, Integer.MAX_VALUE, matcher)
        .forEach(path ->
            putFileStatus(workDir, path, fileStatusMap, context, watchedFiles)
        );
    watchedFiles.forEach(f -> fileStatusMap.put(FileStatus.deleted, f));
    return fileStatusMap;
  }

  private boolean isTagmlFile(final Path workDir, final Path filePath) {
    return workDir.relativize(filePath).startsWith(Paths.get(SOURCE_DIR))
        && fileType(filePath.toString()).equals(FileType.tagmlSource);
  }

  private boolean isViewDefinition(final Path workDir, final Path filePath) {
    return workDir.relativize(filePath).startsWith(Paths.get(VIEWS_DIR))
        && fileType(filePath.toString()).equals(FileType.viewDefinition);
  }

  private void putFileStatus(Path workDir, Path filePath, Multimap<FileStatus, String> fileStatusMap, CLIContext context, Set<String> watchedFiles) {
    String file = workDir.relativize(filePath)
        .toString()
        .replace("\\", "/");
    if (watchedFiles.contains(file)) {
      Instant lastCommit = context.getWatchedFiles().get(file).getLastCommit();
      try {
        Instant lastModified = Files.getLastModifiedTime(filePath).toInstant();
        FileStatus fileStatus = lastModified.isAfter(lastCommit)
            ? FileStatus.changed
            : FileStatus.unchanged;
        fileStatusMap.put(fileStatus, file);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      watchedFiles.remove(file);
    } else {
      fileStatusMap.put(FileStatus.created, file);
    }
  }

  void showChanges(Multimap<FileStatus, String> fileStatusMap) {
    AnsiConsole.systemInstall();

    Set<String> changedFiles = new HashSet<>(fileStatusMap.get(FileStatus.changed));
    Set<String> deletedFiles = new HashSet<>(fileStatusMap.get(FileStatus.deleted));
    if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
      System.out.printf("Uncommitted changes:%n" +
          "  (use \"alexandria commit <file>...\" to commit the selected changes)%n" +
          "  (use \"alexandria commit -a\" to commit all changes)%n" +
          "  (use \"alexandria revert <file>...\" to discard changes)%n%n");
      Set<String> changedOrDeletedFiles = new TreeSet<>();
      changedOrDeletedFiles.addAll(changedFiles);
      changedOrDeletedFiles.addAll(deletedFiles);
      changedOrDeletedFiles.forEach(file -> {
            String status = changedFiles.contains(file)
                ? "        modified: "
                : "        deleted:  ";
            System.out.println(ansi().fg(RED).a(status).a(file).reset());
          }
      );
    }

    Collection<String> createdFiles = fileStatusMap.get(FileStatus.created);
    if (!createdFiles.isEmpty()) {
      System.out.printf("Untracked files:%n" +
          "  (use \"alexandria add <file>...\" to start tracking this file.)%n%n");
      createdFiles.stream().sorted().forEach(f ->
          System.out.println(ansi().fg(RED).a("        ").a(f).reset())
      );
    }

    AnsiConsole.systemUninstall();
  }

}
