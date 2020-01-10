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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huc.di.tag.sparql.SPARQLQueryHandler;
import nl.knaw.huc.di.tag.sparql.SPARQLResult;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.stream.Collectors.joining;

public class SPARQLQueryCommand extends AlexandriaCommand {
  private static final String QUERY = "query";

  public SPARQLQueryCommand() {
    super("query", "Query the document using SPARQL.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("DOCUMENT")
        .metavar("<document>")
        .dest(DOCUMENT)
        .type(String.class)
        .required(true)
        .help("The name of the document to query.");
    subparser
        .addArgument("-q", "--query")
        .metavar("<sparql-file>")
        .dest(QUERY)
        .type(String.class)
        .required(true)
        .help("The file containing the SPARQL query.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkAlexandriaIsInitialized();
    String docName = namespace.getString(DOCUMENT);
    String sparqlFile = namespace.getString(QUERY);
    Path filePath = workFilePath(sparqlFile);
    File file = filePath.toFile();
    if (file.isFile()) {
      try {
        String sparqlQuery = FileUtils.readFileToString(file, Charsets.UTF_8);
        Long docId = getIdForExistingDocument(docName);
        try (TAGStore store = getTAGStore()) {
          store.runInTransaction(
              () -> {
                System.out.printf("document: %s%n%n", docName);
                System.out.printf("query:%n  %s%n%n", sparqlQuery.replaceAll("\\n", "\n  "));
                TAGDocument document = store.getDocument(docId);
                SPARQLQueryHandler h = new SPARQLQueryHandler(document);
                SPARQLResult result = h.execute(sparqlQuery);
                System.out.printf(
                    "result:%n%s%n",
                    result.getValues().stream().map(Object::toString).collect(joining("\n")));
                if (!result.isOk()) {
                  System.out.printf("errors: %s%n", result.getErrors());
                }
              });
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      System.err.printf("%s is not a file!%n", sparqlFile);
    }
  }
}
