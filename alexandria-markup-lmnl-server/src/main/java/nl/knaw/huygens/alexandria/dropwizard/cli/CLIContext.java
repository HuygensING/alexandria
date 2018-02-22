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

import java.util.HashMap;
import java.util.Map;

public class CLIContext {

  static class FileInfo {
    String documentName;
    String viewName;

    void setDocumentName(String documentName) {
      this.documentName = documentName;
    }

    String getDocumentName() {
      return documentName;
    }

    void setViewName(String viewName) {
      this.viewName = viewName;
    }

    String getViewName() {
      return viewName;
    }
  }

  private Map<String, FileInfo> fileContextMap = new HashMap<>();

  public CLIContext setDocumentName(String filename, String docName) {
    fileContextMap.putIfAbsent(filename, new FileInfo());
    fileContextMap.get(filename).setDocumentName(docName);
    return this;
  }

  public String getDocumentName(String filename) {
    return fileContextMap.get(filename).getDocumentName();
  }

  public CLIContext setViewName(String filename, String viewName) {
    fileContextMap.putIfAbsent(filename, new FileInfo());
    fileContextMap.get(filename).setViewName(viewName);
    return this;
  }

  public String getViewName(String filename) {
    return fileContextMap.get(filename).getViewName();
  }

}
