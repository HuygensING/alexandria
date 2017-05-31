package nl.knaw.huygens.alexandria.markup.client;

/*
 * #%L
 * alexandria-markup-lmnl-client
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

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.alexandria.markup.api.AboutInfo;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class OptimisticAlexandriaMarkupClient {
  AlexandriaMarkupClient delegate;

  // constructors

  public OptimisticAlexandriaMarkupClient(final URI alexandriaURI) {
    delegate = new AlexandriaMarkupClient(alexandriaURI);
  }

  public OptimisticAlexandriaMarkupClient(final String alexandriaURI) {
    this(URI.create(alexandriaURI));
  }

  public OptimisticAlexandriaMarkupClient(final URI alexandriaURI, SSLContext sslContext) {
    delegate = new AlexandriaMarkupClient(alexandriaURI, sslContext);
  }

  public OptimisticAlexandriaMarkupClient(final String alexandriaURI, SSLContext sslContext) {
    this(URI.create(alexandriaURI), sslContext);
  }

  // convenience methods

  // delegated methods

  public WebTarget getRootTarget() {
    return delegate.getRootTarget();
  }

  public void close() {
    delegate.close();
  }

  public void setProperty(String jerseyClientProperty, Object value) {
    delegate.setProperty(jerseyClientProperty, value);
  }

  public AboutInfo getAbout() {
    return unwrap(delegate.getAbout());
  }

  public UUID addDocument(String lmnl) {
    return unwrap(delegate.addDocument(lmnl));
  }

  public String getLMNL(UUID uuid) {
    return unwrap(delegate.getLMNL(uuid));
  }

  public void setDocument(UUID uuid, String string) {
    unwrap(delegate.setDocument(uuid, string));
  }

  public String getMarkupDepthLaTex(UUID uuid) {
    return unwrap(delegate.getMarkupDepthLaTex(uuid));
  }

  public String getDocumentLaTeX(UUID uuid) {
    return unwrap(delegate.getDocumentLaTeX(uuid));
  }

  public String getMatrixLaTex(UUID uuid) {
    return unwrap(delegate.getMatrixLaTex(uuid));
  }

  public String getKdTreeLaTex(UUID uuid) {
    return unwrap(delegate.getKdTreeLaTex(uuid));
  }

  public JsonNode postTAGQLQuery(UUID uuid, String string) {
    return unwrap(delegate.postTAGQLQuery(uuid, string));
  }
  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      Optional<Response> response = restResult.getResponse();
      String status = response.isPresent() ? response.get().getStatus() + ": " : "";
      String message = status + restResult.getFailureCause().orElse("Unspecified error");
      throw new AlexandriaException(message);
    }
    return restResult.get();
  }

}
