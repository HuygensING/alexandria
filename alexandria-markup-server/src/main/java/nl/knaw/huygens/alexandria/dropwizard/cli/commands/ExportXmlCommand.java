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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.TAGViews;
import nl.knaw.huc.di.tag.tagml.TAGML;
import nl.knaw.huc.di.tag.tagml.xml.exporter.XMLExporter;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;

import java.io.IOException;
import java.nio.file.Files;

public class ExportXmlCommand extends AlexandriaCommand {

  static final String LEADING_LAYER = "leading_layer";

  public ExportXmlCommand() {
    super("export-xml", "Export the document as xml.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("document")
        .dest(DOCUMENT)
        .metavar("<document>")
        .type(String.class)
        .required(true)
        .help("The name of the document to export.");
    subparser
        .addArgument("-l", "--leadinglayer")
        .dest(LEADING_LAYER)
        .metavar("<leading_layer>")
        .type(String.class)
        .required(false)
        .help("In case of overlapping layers, the layer that defines the xml hierarchy.");
    subparser
        .addArgument("-o", "--outputfile")
        .dest(OUTPUTFILE)
        .metavar("<file>")
        .type(String.class)
        .required(false)
        .help("The file to export to.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkAlexandriaIsInitialized();

    String docName = namespace.getString(DOCUMENT);
    String outputFile = namespace.getString(OUTPUTFILE);
    String leadingLayer = namespace.getString(LEADING_LAYER);
    Long docId = getIdForExistingDocument(docName);
    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(
          () -> {
            CLIContext context = readContext();
            String viewName = context.getActiveView();
            TAGView tagView =
                MAIN_VIEW.equals(viewName)
                    ? TAGViews.getShowAllMarkupView(store)
                    : getExistingView(viewName, store, context);
            TAGDocument document = store.getDocument(docId);
            XMLExporter xmlExporter = new XMLExporter(store, tagView);
            String leading = leadingLayer == null ? TAGML.DEFAULT_LAYER : leadingLayer;
            String xml = xmlExporter.asXML(document, leading);
            if (outputFile != null) {
              writeToFile(outputFile, xml);
            } else {
              System.out.println(xml);
            }
          });
    }
  }

  private void writeToFile(final String file, final String contents) {
    try {
      System.out.printf("exporting to %s...", file);
      Files.write(workFilePath(file), contents.getBytes());
      System.out.println();
      System.out.print("done!");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
