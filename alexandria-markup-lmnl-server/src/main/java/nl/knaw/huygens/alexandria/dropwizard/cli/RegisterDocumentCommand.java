package nl.knaw.huygens.alexandria.dropwizard.cli;

/*
 * #%L
 * alexandria-markup-lmnl-server
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
import nl.knaw.huygens.alexandria.dropwizard.api.NamedDocumentService;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class RegisterDocumentCommand extends AlexandriaCommand {
  private static final Logger LOG = LoggerFactory.getLogger(RegisterDocumentCommand.class);

  public RegisterDocumentCommand() {
    super("register-document", "Parse a document and store it as TAG");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-n", "--name")//
        .dest(NAME)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document");

    subparser.addArgument("-f", "--file")//
        .dest(FILE)//
        .type(String.class)//
        .required(true)//
        .help("The file containing the document source");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    String filename = namespace.getString(FILE);
    String docName = namespace.getString(NAME);
    System.out.println("Parsing " + filename + " to document " + docName + "...");

    try (TAGStore store = new TAGStore(PROJECT_DIR, false)) {
      store.runInTransaction(() -> {
        try {
          LMNLImporter lmnlImporter = new LMNLImporter(store);
          File file = new File(filename);
          FileInputStream fileInputStream = FileUtils.openInputStream(file);
          DocumentWrapper document = lmnlImporter.importLMNL(fileInputStream);
          NamedDocumentService service = new NamedDocumentService(store);
          service.registerDocument(document, docName);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
    System.out.println("done!");
  }
}
