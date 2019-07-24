package nl.knaw.huygens.alexandria.markup.client;

/*
 * #%L
 * alexandria-markup-client
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

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public class OptimisticAlexandriaMarkupClient {
  final AlexandriaMarkupClient delegate;

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

  public AppInfo getAbout() {
    return unwrap(delegate.getAbout());
  }

  public String getTAGML(UUID uuid) {
    return unwrap(delegate.getTAGML(uuid));
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

  public UUID addDocumentFromTexMECS(String string) {
    return unwrap(delegate.addDocumentFromTexMECS(string));
  }

  public void setDocumentFromTexMECS(UUID uuid, String string) {
    unwrap(delegate.setDocumentFromTexMECS(uuid, string));
  }

  public UUID addDocumentFromTAGML(String string) {
    return unwrap(delegate.addDocumentFromTAGML(string));
  }

  public void setDocumentFromTAGML(UUID uuid, String string) {
    unwrap(delegate.setDocumentFromTAGML(uuid, string));
  }

  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      Optional<Response> response = restResult.getResponse();
      String status = response.map(response1 -> response1.getStatus() + ": ").orElse("");
      String message = status + restResult.getFailureCause().orElse("Unspecified error");
      throw new AlexandriaException(message);
    }
    return restResult.get();
  }

}
