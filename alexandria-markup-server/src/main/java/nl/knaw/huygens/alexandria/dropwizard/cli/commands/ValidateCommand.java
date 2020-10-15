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
import nl.knaw.huc.di.tag.schema.TAGMLSchemaFactory;
import nl.knaw.huc.di.tag.schema.TAGMLSchemaParseResult;
import nl.knaw.huc.di.tag.validate.TAGValidationResult;
import nl.knaw.huc.di.tag.validate.TAGValidator;
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class ValidateCommand extends AlexandriaCommand {

  private static final String SCHEMA = "schema";

  public ValidateCommand() {
    super("schema-validate", "Validate a document against a TAG schema.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("DOCUMENT")
        .metavar("<document>")
        .dest(DOCUMENT)
        .type(String.class)
        .required(true)
        .help(
            "The name of the document to validate. It must have a valid URL to a valid schema file (in YAML) defined using '[!schema <schemaLocationURL>]' in the TAGML source file.");
    //    subparser
    //        .addArgument("-s", "--schema")
    //        .dest(SCHEMA)
    //        .type(String.class)
    //        .required(true)
    //        .help("The TAG schema file.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkAlexandriaIsInitialized();

    String docName = namespace.getString(DOCUMENT);
    Long docId = getIdForExistingDocument(docName);

    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(
          () -> {
            TAGDocument document = store.getDocument(docId);
            try {
              final Optional<URL> schemaLocation = document.getSchemaLocation();
              if (schemaLocation.isPresent()) {
                continueWithSchemaLocation(docName, store, document, schemaLocation.get());
              } else {
                throw new AlexandriaCommandException(
                    "There was no schema location defined in "
                        + docName
                        + ", please add\n  [!schema <schemaLocationURL>]\nto the tagml sourcefile.");
              }
            } catch (IOException e) {
              throw new AlexandriaCommandException(
                  "The schema location in " + docName + " is invalid.");
            }
          });
    }
  }

  private void continueWithSchemaLocation(
      final String docName, final TAGStore store, final TAGDocument document, final URL url)
      throws IOException {
    String tmpSchemaLocationURL = url.toString();
    if (url.getProtocol().equals("file") && !tmpSchemaLocationURL.contains("file://")) {
      tmpSchemaLocationURL =
          url.getProtocol()
              + "://"
              + url.getPath(); // because url.toString() somehow loses     '//', but only on
      // Windows?!
    }
    final String schemaLocationURL = tmpSchemaLocationURL;
    System.out.println("Parsing schema from " + schemaLocationURL + ":");
    String schemaYAML = IOUtils.toString(url, Charsets.UTF_8);
    final TAGMLSchemaParseResult schemaParseResult = TAGMLSchemaFactory.parseYAML(schemaYAML);
    if (schemaParseResult.getErrors().isEmpty()) {
      continueWithValidSchema(docName, store, document, schemaParseResult, schemaLocationURL);
    } else {
      System.out.println(
          "  errors:\n"
              + schemaParseResult.getErrors().stream()
                  .map(e -> "  - " + e.replaceAll("\\(StringReader\\)", schemaLocationURL))
                  .collect(joining("\n")));
    }
  }

  private void continueWithValidSchema(
      final String docName,
      final TAGStore store,
      final TAGDocument document,
      final TAGMLSchemaParseResult schemaParseResult,
      final String schemaLocationURL) {
    System.out.println("  done\n");
    TAGValidationResult result =
        new TAGValidator(store).validate(document, schemaParseResult.getSchema());
    System.out.println("Document " + docName + " is ");
    if (!result.isValid()) {
      System.out.println("  not valid:");
      System.out.println(
          result.getErrors().stream().map(e -> "  - error: " + e).collect(joining("\n")));
    } else {
      System.out.println("  valid");
    }
    System.out.println(
        result.getWarnings().stream().map(e -> "  - warning: " + e).collect(joining("\n")));
    System.out.println("according to the schema defined in " + schemaLocationURL);
  }
}
