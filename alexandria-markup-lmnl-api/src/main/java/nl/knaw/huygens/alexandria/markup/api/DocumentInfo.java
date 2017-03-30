package nl.knaw.huygens.alexandria.markup.api;

/*
 * #%L
 * alexandria-markup-lmnl-api
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentInfo {
  Instant created;
  Instant modified;
  private String uriBase;

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
