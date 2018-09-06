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
import nl.knaw.huc.di.tag.model.graph.DotFactory;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
    Map<String, Long> documentIndex = readDocumentIndex();
    String docName = namespace.getString(DOCUMENT);
    Long docId = documentIndex.get(docName);
    store.open();
    store.runInTransaction(() -> {
      System.out.printf("document: %s%n", docName);

      System.out.printf("Retrieving document %s%n", docName);
      TAGDocument document = store.getDocument(docId);

      DotFactory dotFactory = new DotFactory();
      String dot = dotFactory.toDot(document, "");
      String fileName = String.format("%s.%s", docName, getFormat());
      System.out.printf("exporting to file %s...", fileName);
      try {
        render(dot, fileName);
        FileUtils.writeStringToFile(new File(fileName), dot, Charsets.UTF_8);
      } catch (IOException e) {
        e.printStackTrace();
      }
      System.out.println();
      System.out.println("done!");

    });
    store.close();
  }

  protected abstract String getFormat();

  protected abstract void render(final String dot, final String fileName) throws IOException;

}
