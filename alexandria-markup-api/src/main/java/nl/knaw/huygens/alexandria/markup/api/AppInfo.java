package nl.knaw.huygens.alexandria.markup.api;

/*
 * #%L
 * alexandria-markup-api
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
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


public class AppInfo {
  private String appName;
  private String startedAt;
  private String version;
  private String buildDate;
  private String commitId;
  private String scmBranch;

  public AppInfo setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  public String getAppName() {
    return appName;
  }

  public AppInfo setStartedAt(String startedAt) {
    this.startedAt = startedAt;
    return this;
  }

  public String getStartedAt() {
    return startedAt;
  }

  public AppInfo setVersion(String version) {
    this.version = version;
    return this;
  }

  public String getVersion() {
    return version;
  }

  public AppInfo setBuildDate(final String buildDate) {
    this.buildDate = buildDate;
    return this;
  }

  public String getBuildDate() {
    return buildDate;
  }

  public AppInfo setCommitId(final String commitId) {
    this.commitId = commitId;
    return this;
  }

  public String getCommitId() {
    return commitId;
  }

  public AppInfo setScmBranch(final String scmBranch) {
    this.scmBranch = scmBranch;
    return this;
  }

  public String getScmBranch() {
    return scmBranch;
  }
}
