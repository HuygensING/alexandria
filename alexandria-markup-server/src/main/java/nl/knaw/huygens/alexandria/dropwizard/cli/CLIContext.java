package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
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

import nl.knaw.huygens.alexandria.view.TAGViewDefinition;

import java.util.*;

public class CLIContext {

  private String activeView = "-";
  private Set<String> watchedDirectories = new HashSet<>();
  private Map<String, FileInfo> watchedFiles = new HashMap<>();
  private Map<String, TAGViewDefinition> tagViewDefinitions = new HashMap<>();
  private Map<String, DocumentInfo> documentInfo = new HashMap<>();

  public CLIContext setActiveView(final String activeView) {
    this.activeView = activeView;
    return this;
  }

  public String getActiveView() {
    return activeView;
  }

  public Set<String> getWatchedDirectories() {
    return watchedDirectories;
  }

  public void setWatchedDirectories(final Set<String> watchedDirectories) {
    this.watchedDirectories = watchedDirectories;
  }

  public CLIContext setWatchedFiles(final Map<String, FileInfo> watchedFiles) {
    this.watchedFiles = watchedFiles;
    return this;
  }

  public Map<String, FileInfo> getWatchedFiles() {
    return watchedFiles;
  }

  public Map<String, TAGViewDefinition> getTagViewDefinitions() {
    return tagViewDefinitions;
  }

  public CLIContext setTagViewDefinitions(final Map<String, TAGViewDefinition> tagViewDefinitions) {
    this.tagViewDefinitions = tagViewDefinitions;
    return this;
  }

  public Map<String, DocumentInfo> getDocumentInfo() {
    return documentInfo;
  }

  public CLIContext setDocumentInfo(final Map<String, DocumentInfo> documentInfo) {
    this.documentInfo = documentInfo;
    return this;
  }

  public Optional<String> getDocumentName(final String fileName) {
    return documentInfo.values().stream()
        .filter(di -> di.getSourceFile().equals(fileName))
        .findFirst()
        .map(DocumentInfo::getDocumentName);
  }
}
