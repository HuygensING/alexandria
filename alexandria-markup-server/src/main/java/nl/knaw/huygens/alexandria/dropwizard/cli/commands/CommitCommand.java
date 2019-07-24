package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2019 Huygens ING (KNAW)
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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.dropwizard.api.NamedDocumentService;
import nl.knaw.huygens.alexandria.dropwizard.cli.*;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import nl.knaw.huygens.alexandria.view.TAGViewFactory;
import org.apache.commons.io.FileUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommitCommand extends AlexandriaCommand {
  private final String ARG_ALL = "add_all";

  public CommitCommand() {
    super("commit", "Record changes to the repository.");
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
        .metavar("<file>")
        .dest(FILE)//
        .type(String.class)//
        .nargs("*")
        .required(false)//
        .help("the changed file(s)");
    subparser.epilog("Warning: currently, committing changes is only possible in the main view!");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkAlexandriaIsInitialized();
    List<String> fileNames = (namespace.getBoolean(ARG_ALL))
        ? getModifiedWatchedFileNames()
        : relativeFilePaths(namespace);

    try (TAGStore store = getTAGStore()) {
      CLIContext context = readContext();
      fileNames.forEach(fileName -> {
        FileType fileType = fileType(fileName);
        String objectName = fileName;
        switch (fileType) {
          case tagmlSource:
            checkNoViewIsActive();
            objectName = toDocName(fileName);
            processTAGMLFile(context, store, fileName, objectName);
            break;
          case viewDefinition:
            objectName = toViewName(fileName);
            processViewDefinition(store, fileName, objectName, context);
            if (context.getActiveView().equals(objectName)) {
              checkoutView(objectName);
            }
            break;
          case other:
            processOtherFile(fileName);
            break;
        }
        context.getWatchedFiles().put(fileName, new FileInfo()
            .setObjectName(objectName)
            .setFileType(fileType)
            .setLastCommit(Instant.now()));
      });
      storeContext(context);
    }
    System.out.println("done!");
  }

  private void checkNoViewIsActive() {
    String activeView = readContext().getActiveView();
    boolean inMainView = activeView.equals(MAIN_VIEW);
    if (!inMainView) {
      System.out.printf("View %s is active. Currently, committing is only allowed in the main view. Use:%n  alexandria checkout -%nto return to the main view.%n", activeView);
      throw new AlexandriaCommandException("no commit in view allowed");
    }
  }

  private void processTAGMLFile(CLIContext context, TAGStore store, String fileName, String docName) {
    System.out.printf("Parsing %s to document %s...%n", fileName, docName);

    store.runInTransaction(Unchecked.runnable(() -> {
      TAGMLImporter tagmlImporter = new TAGMLImporter(store);
      File file = workFilePath(fileName).toFile();
      FileInputStream fileInputStream = FileUtils.openInputStream(file);
      TAGDocument document = tagmlImporter.importTAGML(fileInputStream);
      NamedDocumentService service = new NamedDocumentService(store);
      service.registerDocument(document, docName);
      DocumentInfo newDocumentInfo = new DocumentInfo()
          .setDocumentName(docName)
          .setSourceFile(fileName)
          .setDbId(document.getDbId());
      context.getDocumentInfo().put(docName, newDocumentInfo);
    }));
  }

  private void processViewDefinition(TAGStore store, String fileName, String viewName, final CLIContext context) {
    Map<String, TAGView> viewMap = readViewMap(store, context);
    File viewFile = workFilePath(fileName).toFile();
    TAGViewFactory viewFactory = new TAGViewFactory(store);
    System.out.printf("Parsing %s to view %s...%n", fileName, viewName);
    try {
      String json = FileUtils.readFileToString(viewFile, Charsets.UTF_8);
      TAGView view = viewFactory.fromJsonString(json);
      viewMap.put(viewName, view);

      storeViewMap(viewMap, context);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void processOtherFile(String fileName) {
  }

  private String toDocName(String fileName) {
    return fileName
        .replaceAll("^.*" + SOURCE_DIR + "/", "")
        .replaceAll(".tag(ml)?", "");
  }

  private String toViewName(String fileName) {
    return fileName
        .replaceAll("^.*" + VIEWS_DIR + "/", "")
        .replaceAll(".json", "");
  }

  private List<String> getModifiedWatchedFileNames() {
    List<String> modifiedFiles = new ArrayList<>();
    CLIContext cliContext = readContext();
    cliContext.getWatchedFiles().forEach((k, v) -> {
      Path filePath = Paths.get(workDir).resolve(k);
      Instant lastCommitted = v.getLastCommit();
      try {
        FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
        if (lastModifiedTime.toInstant().isAfter(lastCommitted)) {
          modifiedFiles.add(k);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    return modifiedFiles;
  }
}
