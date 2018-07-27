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
import nl.knaw.huygens.alexandria.query.TAGQLQueryHandler;
import nl.knaw.huygens.alexandria.query.TAGQLResult;
import nl.knaw.huygens.alexandria.storage.TAGDocument;

import java.util.Map;

import static java.util.stream.Collectors.joining;

public class QueryCommand extends AlexandriaCommand {
  private static final String DOCUMENT = "document";
  private static final String QUERY = "query";

  public QueryCommand() {
    super("query", "Query the document.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to query.");
    subparser.addArgument("-q", "--query")//
        .dest(QUERY)
        .type(String.class)//
        .required(true)//
        .help("The TAGQL query.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    Map<String, Long> documentIndex = readDocumentIndex();
    String docName = namespace.getString(DOCUMENT);
    String statement = namespace.getString(QUERY);
    Long docId = documentIndex.get(docName);
    store.open();
    store.runInTransaction(() -> {
      System.out.printf("document: %s%n", docName);
      System.out.printf("query: %s%n", statement);
      TAGDocument document = store.getDocument(docId);
      TAGQLQueryHandler h = new TAGQLQueryHandler(document);
      TAGQLResult result = h.execute(statement);
      System.out.printf("result:%n%s%n", result.getValues().stream()
          .map(Object::toString)
          .collect(joining("\n")));
      if (!result.isOk()) {
        System.out.printf("errors: %s%n", result.getErrors());
      }
    });
    store.close();
  }
}
