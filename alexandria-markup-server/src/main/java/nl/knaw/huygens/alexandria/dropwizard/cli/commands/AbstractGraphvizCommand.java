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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.model.graph.DotFactory;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;

import java.io.IOException;

abstract class AbstractGraphvizCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";

  public AbstractGraphvizCommand(final String name, final String description) {
    super(name, description);
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to export.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    String docName = namespace.getString(DOCUMENT);
    boolean toFile = false;
    Long docId = getIdForExistingDocument(docName);
    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(() -> {
        TAGDocument document = store.getDocument(docId);
        DotFactory dotFactory = new DotFactory();
        String dot = dotFactory.toDot(document, "");
        if (toFile) {
          String fileName = String.format("%s.%s", docName, getFormat());
          System.out.printf("exporting to file %s...", fileName);
          try {
            renderToFile(dot, fileName);
          } catch (IOException e) {
            e.printStackTrace();
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
