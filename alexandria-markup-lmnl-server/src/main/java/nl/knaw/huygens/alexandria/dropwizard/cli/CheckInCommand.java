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
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.compare.Segment;
import nl.knaw.huygens.alexandria.compare.TAGComparison;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.storage.wrappers.TextNodeWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import static nl.knaw.huygens.alexandria.compare.Segment.Type.aligned;

public class CheckInCommand extends AlexandriaCommand {
  // TODO: committing changes invalidates any current views on the document.
  // we need a last-modified for documents and a created for views
  // if view.created < document.last-modified then commit rejected.
  public CheckInCommand() {
    super("commit", "Merge the changes in the view into the TAG");
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
    CLIContext context = readContext();
    String filename = namespace.getString(FILE);
    System.out.println("Merging changes from " + filename + "...");
    checkFileExists(filename);
    store.runInTransaction(() -> {
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
        if (comparison.hasDifferences()) {
          comparison.mergeChanges();
        } else {
          System.out.println("no changes");
        }

      } catch (IOException e) {
        handleException(e);
      }
    });
  }

}
