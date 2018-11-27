package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

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
//    checkDirectoryIsInitialized();
//    try (TAGStore store = getTAGStore()) {
//      store.runInTransaction(() -> {
//        CLIContext context = readContext();
//
//        String filename = namespace.getString(FILE);
//        String documentName = context.getDocumentName(filename);
//        System.out.printf("Reverting %s%n", filename);
//
//        Long docId = getIdForExistingDocument(documentName);
//        TAGDocument tAGDocument = store.getDocument(docId);
//
//        String viewId = context.getViewName(filename);
//        TAGView tagView = getExistingView(viewId);
//
//        TAGMLExporter tagmlExporter = new TAGMLExporter(store, tagView);
//        String tagml = tagmlExporter.asTAGML(tAGDocument)
//            .replaceAll("\n\\s*\n", "\n")
//            .trim();
//        try {
//          FileUtils.writeStringToFile(new File(filename), tagml, Charsets.UTF_8);
//          context = readContext()//
//              .setDocumentName(filename, documentName)//
//              .setViewName(filename, viewId);
//          storeContext(context);
//        } catch (IOException e) {
//          e.printStackTrace();
//          throw new UncheckedIOException(e);
//        }
//      });
//    }
//    System.out.println("done!");
  }

}
