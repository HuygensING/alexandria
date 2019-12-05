package nl.knaw.huygens.alexandria.markup.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

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

public class AlexandriaMarkupClient implements AutoCloseable {

  public static final String LOCATION = "Location";
  private static final String HEADER_AUTH = "auth";
  private WebTarget rootTarget;
  private final Client client;
  private final URI alexandriaMarkupURI;
  // private boolean autoConfirm = true;

  public AlexandriaMarkupClient(final URI alexandriaMarkupURI) {
    this(alexandriaMarkupURI, null);
  }

  public AlexandriaMarkupClient(final URI alexandriaMarkupURI, SSLContext sslContext) {
    this.alexandriaMarkupURI = alexandriaMarkupURI;
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();

    final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    jacksonProvider.setMapper(objectMapper);

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(50);
    cm.setDefaultMaxPerRoute(50);

    ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
    ClientConfig clientConfig = new ClientConfig(jacksonProvider)
        .connectorProvider(connectorProvider)
        .property(ApacheClientProperties.CONNECTION_MANAGER, cm)
        .property(ClientProperties.CONNECT_TIMEOUT, 60000)
        .property(ClientProperties.READ_TIMEOUT, 60000);

    if (sslContext == null) {
      if ("https".equals(alexandriaMarkupURI.getScheme())) {
        throw new RuntimeException("SSL connections need an SSLContext, use: new AlexandriaClient(uri, sslContext) instead.");
      }
      client = ClientBuilder.newClient(clientConfig);

    } else {
      client = ClientBuilder.newBuilder()
          .sslContext(sslContext)
          .withConfig(clientConfig)
          .build();
    }
    rootTarget = client.target(alexandriaMarkupURI);
  }

  @Override
  public void close() {
    client.close();
  }

  public void register(Object component) {
    client.register(component);
    rootTarget = client.target(alexandriaMarkupURI);
  }

  public void setProperty(final String jerseyClientProperty, final Object value) {
    client.property(jerseyClientProperty, value);
    rootTarget = client.target(alexandriaMarkupURI);
  }

  // Alexandria Markup API methods

  public RestResult<AppInfo> getAbout() {
    WebTarget path = rootTarget
        .path(ResourcePaths.ABOUT);
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<AppInfo> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester
        .onStatus(Status.OK, this::toAboutInfoRestResult)
        .getResult();
  }

  public RestResult<Void> setDocumentFromTAGML(UUID documentUUID, String tagml) {
    final WebTarget path = documentTarget(documentUUID).path("tagml");
    return setDocument(tagml, path);
  }

  public RestResult<Void> setDocumentFromTexMECS(UUID documentUUID, String texMECS) {
    final WebTarget path = documentTarget(documentUUID).path("tagml");
    return setDocument(texMECS, path);
  }

  private RestResult<Void> setDocument(String serializedDocument, final WebTarget path) {
    final Entity<String> entity = Entity.entity(serializedDocument, MediaType.TEXT_PLAIN);
    final Supplier<Response> responseSupplier = anonymousPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester
        .onStatus(Status.CREATED, voidRestResult())
        .onStatus(Status.NO_CONTENT, voidRestResult())
        .getResult();
  }

  public RestResult<UUID> addDocumentFromTAGML(String tagml) {
    final WebTarget path = documentsTarget().path("tagml");
    return addDocument(tagml, path);
  }

  public RestResult<UUID> addDocumentFromTexMECS(String texMECS) {
    final WebTarget path = documentsTarget().path("texmecs");
    return addDocument(texMECS, path);
  }

  private RestResult<UUID> addDocument(String serializedDocument, final WebTarget path) {
    final Entity<String> entity = Entity.entity(serializedDocument, UTF8MediaType.TEXT_PLAIN);
    final Supplier<Response> responseSupplier = anonymousPost(path, entity);
    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)
        .getResult();
  }

  public RestResult<String> getTAGML(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_TAGML);
    return stringResult(path);
  }

  public RestResult<String> getDocumentLaTeX(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_LATEX);
    return stringResult(path);
  }

  public RestResult<String> getMarkupDepthLaTex(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_MARKUPDEPTH);
    return stringResult(path);
  }

  public RestResult<String> getMatrixLaTex(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_MATRIX);
    return stringResult(path);
  }

  public RestResult<String> getKdTreeLaTex(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_KDTREE);
    return stringResult(path);
  }

  public RestResult<JsonNode> postTAGQLQuery(UUID documentUUID, String query) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_QUERY);
    final Entity<String> entity = Entity.entity(query, UTF8MediaType.TEXT_PLAIN);
    final Supplier<Response> responseSupplier = anonymousPost(path, entity);
    final RestRequester<JsonNode> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester
        .onStatus(Status.OK, this::toJsonObjectRestResult)
        .getResult();
  }

  // private methods
  private WebTarget documentTarget(UUID documentUUID) {
    return documentsTarget()
        .path(documentUUID.toString());
  }

  private WebTarget documentsTarget() {
    return rootTarget
        .path(ResourcePaths.DOCUMENTS);
  }

  private RestResult<String> toStringRestResult(final Response response) {
    return toEntityRestResult(response, String.class);
  }

  private RestResult<AppInfo> toAboutInfoRestResult(final Response response) {
    return toEntityRestResult(response, AppInfo.class);
  }

  private RestResult<JsonNode> toJsonObjectRestResult(final Response response) {
    return toEntityRestResult(response, JsonNode.class);
  }

  private <E> RestResult<E> toEntityRestResult(final Response response, final Class<E> entityClass) {
    final RestResult<E> result = new RestResult<>();
    final E cargo = response.readEntity(entityClass);
    result.setCargo(cargo);
    return result;
  }

  private RestResult<URI> uriFromLocationHeader(final Response response) {
    final RestResult<URI> result = new RestResult<>();
    final String location = response.getHeaderString(LOCATION);
    final URI uri = URI.create(location);
    result.setCargo(uri);
    return result;
  }

  private RestResult<UUID> uuidFromLocationHeader(final Response response) {
    final RestResult<UUID> result = new RestResult<>();
    final String location = response.getHeaderString(LOCATION);
    final UUID uuid = UUID.fromString(location.replaceFirst(".*/", ""));
    result.setCargo(uuid);
    return result;
  }

  private Supplier<Response> anonymousGet(final WebTarget target) {
    return () -> target.request().get();
  }

  private Supplier<Response> anonymousPut(final WebTarget target, final Entity<?> entity) {
    return () -> target.request()
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .put(entity);
  }

  private Supplier<Response> authorizedPut(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).put(entity);
  }

  private Supplier<Response> anonymousPost(final WebTarget target, final Entity<?> entity) {
    return () -> target.request()
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .post(entity);
  }

  private Supplier<Response> authorizedPost(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).post(entity);
  }

  private Supplier<Response> authorizedDelete(final WebTarget path) {
    return () -> authorizedRequest(path).delete();
  }

  private SyncInvoker authorizedRequest(final WebTarget target) {
    String authHeader = "";
    return target.request()
        .accept(MediaType.APPLICATION_JSON_TYPE)
        .header(HEADER_AUTH, authHeader);
  }

  private RestResult<String> stringResult(WebTarget path) {
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<String> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester
        .onStatus(Status.OK, this::toStringRestResult)
        .getResult();
  }

  private Function<Response, RestResult<Void>> voidRestResult() {
    return (response) -> {
      response.bufferEntity(); // to notify connectors, such as the ApacheConnector, that the entity has been "consumed" and that it should release the current connection back into the Apache
      // ConnectionManager pool (if being used). https://java.net/jira/browse/JERSEY-3149
      return new RestResult<>();
    };
  }

  private WebTarget getDocumentTarget() {
    return rootTarget
        .path(ResourcePaths.DOCUMENTS)
    ;
  }

  public WebTarget getRootTarget() {
    return rootTarget;
  }
}
