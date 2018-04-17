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
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

public class RevertCommand extends AlexandriaCommand {

  public RevertCommand() {
    super("revert", "Revert the changes in the view.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("file")//
        .dest(FILE)//
        .type(String.class)//
        .required(true)//
        .help("The file to be reset");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    store.runInTransaction(() -> {
      CLIContext context = readContext();

      String filename = namespace.getString(FILE);
      String documentName = context.getDocumentName(filename);
      System.out.printf("Reverting %s%n", filename);

      Long documentId = readDocumentIndex().get(documentName);
      DocumentWrapper documentWrapper = store.getDocumentWrapper(documentId);

      String viewId = context.getViewName(filename);
      TAGView tagView = readViewMap().get(viewId);

      LMNLExporter lmnlExporter = new LMNLExporter(store, tagView);
      String lmnl = lmnlExporter.toLMNL(documentWrapper)
          .replaceAll("\n\\s*\n", "\n")
          .trim();
      try {
        FileUtils.writeStringToFile(new File(filename), lmnl, Charsets.UTF_8);
        context = readContext()//
            .setDocumentName(filename, documentName)//
            .setViewName(filename, viewId);
        storeContext(context);
      } catch (IOException e) {
        e.printStackTrace();
        throw new UncheckedIOException(e);
      }
    });
    System.out.println("done!");
  }

}
