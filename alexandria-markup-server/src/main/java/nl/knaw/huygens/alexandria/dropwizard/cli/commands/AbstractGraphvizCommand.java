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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.model.graph.DotFactory;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;

import java.io.IOException;

abstract class AbstractGraphvizCommand extends AlexandriaCommand {

  public AbstractGraphvizCommand(final String name, final String description) {
    super(name, description);
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("DOCUMENT")
        .metavar("<document>")
        .dest(DOCUMENT)
        .type(String.class)
        .required(true)
        .help("The name of the document to export.");
    subparser.addArgument("-o", "--outputfile")
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
    String fileName = namespace.getString(OUTPUTFILE);
    Long docId = getIdForExistingDocument(docName);
    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(() -> {
        TAGDocument document = store.getDocument(docId);
        DotFactory dotFactory = new DotFactory(); // TODO: add option to export using view
        String dot = dotFactory.toDot(document, "");
        if (fileName != null) {
          System.out.printf("exporting to %s...", fileName);
          try {
            renderToFile(dot, fileName);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          System.out.println();
          System.out.println("done!");
        } else {
          renderToStdOut(dot);
        }
      });
    }
  }

  protected abstract String getFormat();

  protected abstract void renderToFile(final String dot, final String fileName) throws IOException;

  protected abstract void renderToStdOut(final String dot);

}
