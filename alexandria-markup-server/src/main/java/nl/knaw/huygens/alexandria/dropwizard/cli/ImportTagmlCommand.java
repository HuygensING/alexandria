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
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.dropwizard.api.NamedDocumentService;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.storage.dto.TAGDocumentDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.Map;

public class ImportTagmlCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";

  public ImportTagmlCommand() {
    super("import-tagml", "Import the document as tagml.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to import.");

    subparser.addArgument("-f", "--file")//
        .dest(FILE)//
        .type(String.class)//
        .required(false)//
        .help("The file containing the document TAGML source");
    // if there are views checked out, they might need to be refreshed
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    Map<String, Long> documentIndex = readDocumentIndex();
    String docName = namespace.getString(DOCUMENT);
    Long docId = getIdForExistingDocument(docName);

    String filename = StringUtils.defaultIfBlank(namespace.getString(FILE), docName + ".tagml");
    System.out.printf("Parsing and importing %s into document %s...%n", filename, docName);
    try (TAGStore store = new TAGStore(ALEXANDRIA_DIR, false)) {
      store.runInTransaction(Unchecked.runnable(() -> {
        TAGMLImporter tagmlImporter = new TAGMLImporter(store);
        File file = new File(filename);
        FileInputStream fileInputStream = FileUtils.openInputStream(file);
        TAGDocument document = tagmlImporter.importTAGML(fileInputStream);
        NamedDocumentService service = new NamedDocumentService(store);
        service.registerDocument(document, docName);
        documentIndex.put(docName, document.getDbId());
        storeDocumentIndex(documentIndex);
        TAGDocumentDTO oldDocumentDTO = store.getDocumentDTO(docId);
        Date creationDate = oldDocumentDTO.getCreationDate();
        document.getDTO().setCreationDate(creationDate);
        store.persist(document.getDTO());
        store.remove(oldDocumentDTO);
      }));
    }
    System.out.println("done!");
  }


}
