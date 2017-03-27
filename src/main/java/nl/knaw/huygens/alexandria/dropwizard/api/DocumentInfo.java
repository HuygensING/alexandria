package nl.knaw.huygens.alexandria.dropwizard.api;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.RootPaths;

public class DocumentInfo {
  Instant created;
  Instant modified;
  private String uriBase;

  public DocumentInfo(UUID documentId, String baseURL) {
    this.uriBase = baseURL + "/" + RootPaths.DOCUMENTS + "/" + documentId + "/";
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
    return URI.create(uriBase + DocumentsResource.SubPaths.LMNL);
  }

  @JsonProperty("^overview")
  public URI getLaTeX1() {
    return URI.create(uriBase + DocumentsResource.SubPaths.LATEX);
  }

  @JsonProperty("^markupdepth")
  public URI getMarkupDepthURI() {
    return URI.create(uriBase + DocumentsResource.SubPaths.MARKUPDEPTH);
  }

  @JsonProperty("^matrix")
  public URI getMatrixURI() {
    return URI.create(uriBase + DocumentsResource.SubPaths.MATRIX);
  }

  @JsonProperty("^kdtree")
  public URI getKdTreeURI() {
    return URI.create(uriBase + DocumentsResource.SubPaths.KDTREE);
  }
}
