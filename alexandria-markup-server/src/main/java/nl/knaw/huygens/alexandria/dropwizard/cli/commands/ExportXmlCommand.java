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
import nl.knaw.huc.di.tag.TAGViews;
import nl.knaw.huc.di.tag.tagml.xml.exporter.XMLExporter;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExportXmlCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";
  private static final String VIEW = "view";

  public ExportXmlCommand() {
    super("export-xml", "Export (the optional view of) the document as xml.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to export.");
    subparser.addArgument("-v", "--view")//
        .dest(VIEW)
        .type(String.class)//
        .required(false)//
        .help("The name of the view to use.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    String docName = namespace.getString(DOCUMENT);
    String viewName = namespace.getString(VIEW);
    boolean useView = viewName != null;
    Long docId = getIdForExistingDocument(docName);
    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(() -> {
        System.out.printf("document: %s%n", docName);
        System.out.printf("Retrieving document %s%n", docName);
        TAGDocument document = store.getDocument(docId);
        String format = "xml";

        TAGView tagView;
        if (useView) {
          System.out.printf("Retrieving view %s%n", viewName);
          tagView = getExistingView(viewName, store);
        } else {
          tagView = TAGViews.getShowAllMarkupView(store);
        }

        String sub = useView ? "-" + viewName : "";
        String fileName = String.format("%s%s.%s", docName, sub, format);
        System.out.printf("exporting to file %s...", fileName);
        try {
          XMLExporter xmlExporter = new XMLExporter(store, tagView);
          String xml = xmlExporter.asXML(document);
          FileUtils.writeStringToFile(new File(fileName), xml, Charsets.UTF_8);
        } catch (IOException e) {
          e.printStackTrace();
        }
        System.out.println();
        System.out.println("done!");
      });
    }
  }

}
