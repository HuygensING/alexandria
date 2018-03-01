package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
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
import nl.knaw.huygens.alexandria.compare.TAGComparison;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.wrappers.DocumentWrapper;
import nl.knaw.huygens.alexandria.view.TAGView;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public abstract class AbstractCompareCommand extends AlexandriaCommand {

  public AbstractCompareCommand(final String name, final String description) {
    super(name, description);
  }

  String getDocumentName(final String filename, final CLIContext context) {
    String documentName = null;
    documentName = context.getDocumentName(filename);
    return documentName;
  }

  DocumentWrapper getDocumentWrapper(final String documentName) {
    Long documentId = readDocumentIndex().get(documentName);
    return store.getDocumentWrapper(documentId);
  }

  TAGComparison getTAGComparison(final DocumentWrapper original, final TAGView tagView, final String editedFileName) throws IOException {
    File editedFile = new File(editedFileName);
    String newLMNL = FileUtils.readFileToString(editedFile, Charsets.UTF_8);
    LMNLImporter importer = new LMNLImporter(store);
    DocumentWrapper edited = importer.importLMNL(newLMNL);

    return new TAGComparison(original, tagView, edited);
  }

}
