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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class ExportTagmlCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";

  public ExportTagmlCommand() {
    super("export-tagml", "Export the document as tagml.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to export.");
    subparser.addArgument("-f", "--file")//
        .dest(FILE)//
        .type(String.class)//
        .required(false)//
        .help("The file to export the TAGML to.");

  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    catchExceptions(() -> {
      checkDirectoryIsInitialized();
      String docName = namespace.getString(DOCUMENT);
      Long docId = getIdForExistingDocument(docName);
      try (TAGStore store = getTAGStore()) {
        store.runInTransaction(() -> {
          System.out.printf("document: %s%n", docName);

          System.out.printf("Retrieving document %s%n", docName);
          TAGDocument document = store.getDocument(docId);

          String fileName = StringUtils.defaultIfBlank(namespace.getString(FILE), docName + ".tagml");
          System.out.printf("exporting to file %s...", fileName);
          try {
            TAGMLExporter tagmlExporter = new TAGMLExporter(store);
            String tagml = tagmlExporter.asTAGML(document);
            FileUtils.writeStringToFile(new File(fileName), tagml, Charsets.UTF_8);
          } catch (IOException e) {
            e.printStackTrace();
          }
          System.out.println();
          System.out.println("done!");

        });
      }
    });
  }

}
