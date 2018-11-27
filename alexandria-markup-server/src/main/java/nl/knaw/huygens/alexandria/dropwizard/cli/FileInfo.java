package nl.knaw.huygens.alexandria.dropwizard.cli;

import java.time.Instant;

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
class FileInfo {
  private FileType fileType;
  private String objectName;
  private Instant lastCommit;

  public FileType getFileType() {
    return fileType;
  }

  public FileInfo setFileType(FileType fileType) {
    this.fileType = fileType;
    return this;
  }

  public String getObjectName() {
    return objectName;
  }

  public FileInfo setObjectName(String objectName) {
    this.objectName = objectName;
    return this;
  }

  public Instant getLastCommit() {
    return lastCommit;
  }

  public FileInfo setLastCommit(Instant lastCommit) {
    this.lastCommit = lastCommit;
    return this;
  }
}
