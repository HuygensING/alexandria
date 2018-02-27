package nl.knaw.huygens.alexandria.markup.api;

/*
 * #%L
 * alexandria-markup-lmnl-api
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


import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentInfo {
  Instant created;
  Instant modified;
  private final String uriBase;

  public DocumentInfo(UUID documentId, String baseURL) {
    this.uriBase = baseURL + "/" + ResourcePaths.DOCUMENTS + "/" + documentId + "/";
  }

  public DocumentInfo setCreated(Instant created) {
    this.created = created;
    return this;
  }

  public String getCreated() {
    return created.toString();
  }

  public DocumentInfo setModified(Instant modified) {
    this.modified = modified;
    return this;
  }

  public String getModified() {
    return modified.toString();
  }

  @JsonProperty("^lmnl")
  public URI getLMNLURI() {
    return URI.create(uriBase + ResourcePaths.DOCUMENTS_LMNL);
  }

  @JsonProperty("^overview")
  public URI getLaTeX1() {
    return URI.create(uriBase + ResourcePaths.DOCUMENTS_LATEX);
  }

  @JsonProperty("^markupdepth")
  public URI getMarkupDepthURI() {
    return URI.create(uriBase + ResourcePaths.DOCUMENTS_MARKUPDEPTH);
  }

  @JsonProperty("^matrix")
  public URI getMatrixURI() {
    return URI.create(uriBase + ResourcePaths.DOCUMENTS_MATRIX);
  }

  @JsonProperty("^kdtree")
  public URI getKdTreeURI() {
    return URI.create(uriBase + ResourcePaths.DOCUMENTS_KDTREE);
  }
}
