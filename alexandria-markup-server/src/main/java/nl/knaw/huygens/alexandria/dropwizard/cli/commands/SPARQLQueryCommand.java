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
    subparser.addArgument("-d", "--document")//
        .dest(DOCUMENT)//
        .type(String.class)//
        .required(true)//
        .help("The name of the document to query.");
    subparser.addArgument("-q", "--query")//
        .dest(QUERY)
        .type(String.class)//
        .required(true)//
        .help("The file containing the SPARQL query.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
//    System.out.println("Resource annotationP = TAG.annotation;");
//    Resource annotationP = TAG.annotation;
//    System.out.println("Resource alt = RDF.Alt;");
//    Resource alt = RDF.Alt;
//    System.out.println("String uri = SKOS.getURI();");
//    String uri = SKOS.getURI();
//    System.out.println("Property altLabel = SKOS.altLabel");
//    Property altLabel = SKOS.altLabel;
//    System.out.println("Resource collection = SKOS.Collection;");
//    Resource collection = SKOS.Collection;
//    System.out.println("Resource annotation = TAG.Annotation");
//    Resource annotation = TAG.Annotation;
    checkDirectoryIsInitialized();
    String docName = namespace.getString(DOCUMENT);
    String sparqlFile = namespace.getString(QUERY);
    Path filePath = workFilePath(sparqlFile);
    File file = filePath.toFile();
    if (file.isFile()) {
      try {
        String sparqlQuery = FileUtils.readFileToString(file, Charsets.UTF_8);
        Long docId = getIdForExistingDocument(docName);
        try (TAGStore store = getTAGStore()) {
          store.runInTransaction(() -> {
            System.out.printf("document: %s%n", docName);
            System.out.printf("query:%n  %s%n", sparqlQuery.replaceAll("\\n", "\n  "));
            TAGDocument document = store.getDocument(docId);
            SPARQLQueryHandler h = new SPARQLQueryHandler(document);
            SPARQLResult result = h.execute(sparqlQuery);
            System.out.printf("result:%n%s%n", result.getValues().stream()
                .map(Object::toString)
                .collect(joining("\n")));
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
