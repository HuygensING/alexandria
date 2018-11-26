package nl.knaw.huygens.alexandria.dropwizard.cli;

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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.dropwizard.api.NamedDocumentService;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitCommand extends AlexandriaCommand {
  public static final String ARG_FILE = "file";
  private final String ARG_ALL = "add_all";

  enum FileType {
    viewDefinition, tagmlSource, other
  }

  public CommitCommand() {
    super("commit", "Record changes to the repository");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-a")
        .dest(ARG_ALL)
        .action(Arguments.storeTrue())
        .setDefault(false)
        .required(false)
        .help("automatically add all changed files");
    subparser.addArgument(ARG_FILE)//
        .metavar("FILE")
        .dest(FILE)//
        .type(String.class)//
        .nargs("*")
        .required(false)//
        .help("the changed file(s)");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    List<String> fileNames = (namespace.getBoolean(ARG_ALL))
        ? getModifiedWatchedFileNames()
        : namespace.getList(ARG_FILE);
    Map<String, Long> documentIndex = readDocumentIndex();
    try (TAGStore store = getTAGStore()) {
      fileNames.forEach(fileName -> {
        switch (fileType(fileName)) {
          case tagmlSource:
            processTAGMLFile(documentIndex, store, fileName);
            break;
          case viewDefinition:
            processViewDefinition(documentIndex, store, fileName);
            break;
          case other:
            processOtherFile(documentIndex, store, fileName);
            break;
        }
        CLIContext cliContext = readContext();
        cliContext.getWatchedFiles().put(fileName, Instant.now());
        storeContext(cliContext);
      });
    }
    System.out.println("done!");
  }


  private FileType fileType(String fileName) {
    if (fileName.endsWith(".tagml") || fileName.endsWith(".tag")) {
      return FileType.tagmlSource;
    }
    if (fileName.endsWith(".json")) {
      return FileType.viewDefinition;
    }
    return FileType.other;
  }

  private void processTAGMLFile(Map<String, Long> documentIndex, TAGStore store, String fileName) {
    String docName = toDocName(fileName);
    System.out.printf("Parsing %s to document %s...%n", fileName, docName);

    store.runInTransaction(Unchecked.runnable(() -> {
      TAGMLImporter tagmlImporter = new TAGMLImporter(store);
      File file = workFilePath(fileName).toFile();
      FileInputStream fileInputStream = FileUtils.openInputStream(file);
      TAGDocument document = tagmlImporter.importTAGML(fileInputStream);
      NamedDocumentService service = new NamedDocumentService(store);
      service.registerDocument(document, docName);
      documentIndex.put(docName, document.getDbId());
      storeDocumentIndex(documentIndex);
    }));
  }

  private void processViewDefinition(Map<String, Long> documentIndex, TAGStore store, String fileName) {

  }

  private void processOtherFile(Map<String, Long> documentIndex, TAGStore store, String fileName) {

  }


  private String toDocName(String fileName) {
    return fileName.replaceAll(".tag(ml)?", "");
  }

  private List<String> getModifiedWatchedFileNames() {
    List<String> modifiedFiles = new ArrayList<>();
    CLIContext cliContext = readContext();
    cliContext.getWatchedFiles().forEach((k, v) -> {
      Path filePath = workFilePath(k);
      Instant lastCommitted = v;
      try {
        FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
        if (!lastModifiedTime.toInstant().equals(lastCommitted)) {
          modifiedFiles.add(k);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    return modifiedFiles;
  }
}
