package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class CLIContext {

  @JsonProperty("fileContextMap")
  private String activeView = "-";
  private Map<String, FileInfo> watchedFiles = new HashMap<>();

  public CLIContext setActiveView(final String activeView) {
    this.activeView = activeView;
    return this;
  }

  public String getActiveView() {
    return activeView;
  }

  public CLIContext setWatchedFiles(final Map<String, FileInfo> watchedFiles) {
    this.watchedFiles = watchedFiles;
    return this;
  }

  public Map<String, FileInfo> getWatchedFiles() {
    return watchedFiles;
  }

}
