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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import static java.util.stream.Collectors.joining;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.compare.TAGComparison;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DiffCommand extends AlexandriaCommand {

  public DiffCommand() {
    super("diff", "Show the changes made to the view.");
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
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    store.runInTransaction(() -> {
      CLIContext context = readContext();

      String filename = namespace.getString(FILE);
      String documentName = context.getDocumentName(filename);
      Long documentId = readDocumentIndex().get(documentName);
      DocumentWrapper original = store.getDocumentWrapper(documentId);

      String viewId = context.getViewName(filename);
      TAGView tagView = readViewMap().get(viewId);

      File editedFile = new File(filename);
      try {
        String newLMNL = FileUtils.readFileToString(editedFile, Charsets.UTF_8);
        LMNLImporter importer = new LMNLImporter(store);
        DocumentWrapper edited = importer.importLMNL(newLMNL);

        TAGComparison comparison = new TAGComparison(original, tagView, edited);

        System.out.printf("diff for document %s, using view %s%n", documentName, viewId);
        if (comparison.hasDifferences()) {
          System.out.printf("%s%n", comparison.getDiffLines().stream().collect(joining("\n")));
        } else {
          System.out.println("no changes");
        }

      } catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException(e);
      }
    });

  }
}
