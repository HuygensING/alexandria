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
import nl.knaw.huc.di.tag.schema.TAGMLSchemaFactory;
import nl.knaw.huc.di.tag.schema.TAGMLSchemaParseResult;
import nl.knaw.huc.di.tag.validate.TAGValidationResult;
import nl.knaw.huc.di.tag.validate.TAGValidator;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.util.stream.Collectors.joining;

public class ValidateCommand extends AlexandriaCommand {

  private static final String SCHEMA = "schema";

  public ValidateCommand() {
    super("schema-validate", "Validate a document against a TAG schema.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("-d", "--document") //
        .dest(DOCUMENT) //
        .type(String.class) //
        .required(true) //
        .help("The name of the document to validate.");
    subparser
        .addArgument("-s", "--schema") //
        .dest(SCHEMA)
        .type(String.class) //
        .required(true) //
        .help("The TAG schema file.");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkAlexandriaIsInitialized();

    String docName = namespace.getString(DOCUMENT);
    Long docId = getIdForExistingDocument(docName);

    String schemaFile = namespace.getString(SCHEMA);
    Path filePath = workFilePath(schemaFile);
    File file = filePath.toFile();
    if (file.isFile()) {
      String schemaYAML = FileUtils.readFileToString(file, Charsets.UTF_8);
      final TAGMLSchemaParseResult schemaParseResult = TAGMLSchemaFactory.parseYAML(schemaYAML);

      CLIContext context = readContext();

      try (TAGStore store = getTAGStore()) {
        TAGValidationResult result =
            store.runInTransaction(
                () -> {
                  TAGDocument document = store.getDocument(docId);
                  return new TAGValidator(store).validate(document, schemaParseResult.schema);
                });

        System.out.println("Document " + docName + " is ");
        if (!result.isValid()) {
          System.out.println("  not valid:");
          System.out.println(
              result.errors.stream().map(e -> "  - error: " + e).collect(joining("\n")));
        } else {
          System.out.println("  valid");
        }
        System.out.println("according to the schema defined in " + schemaFile);
        System.out.println(
            result.warnings.stream().map(e -> "  - warning: " + e).collect(joining("\n")));
      }
    } else {
      System.err.printf("%s is not a file!%n", schemaFile);
    }
  }
}
