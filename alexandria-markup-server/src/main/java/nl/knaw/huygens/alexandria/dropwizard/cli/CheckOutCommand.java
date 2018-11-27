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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;

public class CheckOutCommand extends AlexandriaCommand {
  private static final String VIEW = "view";
  private static final String DOCUMENT = "document";

  public CheckOutCommand() {
    super("checkout", "Activate or deactivate a view in this directory");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("view")//
        .metavar("VIEW")
        .dest(VIEW)//
        .type(String.class)//
        .required(true)//
        .help("The name of the view to use");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();

    String viewName = namespace.getString(VIEW);

    System.out.printf("Checking out view %s...%n", viewName);
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

      store.runInTransaction(() -> {
        watchedTranscriptions.forEach((fileName,fileInfo)->{
          System.out.printf("  %s...%n", fileName);

        });
        TAGDocument document = store.getDocument(docId);

        System.out.printf("Retrieving view %s%n", viewName);
        TAGView tagView = getExistingView(viewName);

        System.out.printf("Exporting document view to %s%n", outFilename);
        TAGMLExporter tagmlExporter = new TAGMLExporter(store, tagView);
        String tagml = tagmlExporter.asTAGML(document)
            .replaceAll("\n\\s*\n", "\n")
            .trim();
        try {
          FileUtils.writeStringToFile(new File(outFilename), tagml, Charsets.UTF_8);
//          CLIContext context = readContext()//
//              .setDocumentName(outFilename, docName)//
//              .setViewName(outFilename, viewName);
//          storeContext(context);
        } catch (IOException e) {
          e.printStackTrace();
          throw new UncheckedIOException(e);
        }
      });
    }

    System.out.println("done!");
  }

}

