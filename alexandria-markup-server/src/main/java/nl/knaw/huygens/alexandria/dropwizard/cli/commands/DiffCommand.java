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
import nl.knaw.huc.di.tag.TAGViews;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.compare.TAGComparison;
import nl.knaw.huygens.alexandria.compare.TAGComparison2;
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class DiffCommand extends AlexandriaCommand {

  public DiffCommand() {
    super("diff", "Show the changes made to the file.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("file")//
        .dest(FILE)//
        .type(String.class)//
        .required(true)//
        .help("The file containing the edited view");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    try (TAGStore store = getTAGStore()) {
      store.runInTransaction(() -> {
        CLIContext context = readContext();

        String filename = namespace.getString(FILE);
        Optional<String> documentName = context.getDocumentName(filename);
        if (documentName.isPresent()) {
          doDiff(store, context, filename, documentName);
        } else {
          throw new AlexandriaCommandException("No document registered for " + filename);
        }
      });
    }
  }

  private void doDiff(final TAGStore store, final CLIContext context, final String filename, final Optional<String> documentName) {
    Long documentId = getIdForExistingDocument(documentName.get());
    TAGDocument original = store.getDocument(documentId);

    String viewName = context.getActiveView();
    TAGView tagView = MAIN_VIEW.equals(viewName)
        ? TAGViews.getShowAllMarkupView(store)
        : getExistingView(viewName, store, context);

    File editedFile = workFilePath(filename).toFile();
    try {
      String newTAGML = FileUtils.readFileToString(editedFile, StandardCharsets.UTF_8);
      TAGMLImporter importer = new TAGMLImporter(store);
      TAGDocument edited = importer.importTAGML(newTAGML);

      TAGComparison comparison = new TAGComparison(original, tagView, edited);
      TAGComparison2 comparison2 = new TAGComparison2(original, tagView, edited, store);

      if (MAIN_VIEW.equals(viewName)) {
        System.out.printf("diff for %s:%n", filename);
      } else {
        System.out.printf("diff for %s, using view %s:%n", filename, viewName);
      }
      if (comparison.hasDifferences()) {
        System.out.printf("%s%n", String.join(System.lineSeparator(), comparison.getDiffLines()));
      } else {
        System.out.println("no changes");
      }
      System.out.printf("%nmarkup diff:%n", filename);
      if (comparison2.hasDifferences()) {
        System.out.printf("%s%n\t", String.join(System.lineSeparator() + "\t", comparison2.getDiffLines()));
      } else {
        System.out.println("no changes");
      }

    } catch (IOException e) {
      e.printStackTrace();
      throw new UncheckedIOException(e);
    }
  }
}
