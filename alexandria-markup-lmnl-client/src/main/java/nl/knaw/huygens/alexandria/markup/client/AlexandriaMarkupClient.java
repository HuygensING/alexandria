package nl.knaw.huygens.alexandria.markup.client;

import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.SyncInvoker;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import nl.knaw.huygens.alexandria.markup.api.AboutInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType;

/*
 * #%L
 * alexandria-java-client
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

public class AlexandriaMarkupClient implements AutoCloseable {

  public static final String LOCATION = "Location";
  private static final String HEADER_AUTH = "auth";
  private WebTarget rootTarget;
  private String authHeader = "";
  private final Client client;
  private final URI alexandriaMarkupURI;
  // private boolean autoConfirm = true;

  public AlexandriaMarkupClient(final URI alexandriaMarkupURI) {
    this(alexandriaMarkupURI, null);
  }

  public AlexandriaMarkupClient(final URI alexandriaMarkupURI, SSLContext sslContext) {
    this.alexandriaMarkupURI = alexandriaMarkupURI;
    final ObjectMapper objectMapper = new ObjectMapper()//
        .registerModule(new Jdk8Module())//
        .registerModule(new JavaTimeModule());

    final JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
    jacksonProvider.setMapper(objectMapper);

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(50);
    cm.setDefaultMaxPerRoute(50);

    ApacheConnectorProvider connectorProvider = new ApacheConnectorProvider();
    ClientConfig clientConfig = new ClientConfig(jacksonProvider)//
        .connectorProvider(connectorProvider)//
        .property(ApacheClientProperties.CONNECTION_MANAGER, cm)//
        .property(ClientProperties.CONNECT_TIMEOUT, 60000)//
        .property(ClientProperties.READ_TIMEOUT, 60000);

    if (sslContext == null) {
      if ("https".equals(alexandriaMarkupURI.getScheme())) {
        throw new RuntimeException("SSL connections need an SSLContext, use: new AlexandriaClient(uri, sslContext) instead.");
      }
      client = ClientBuilder.newClient(clientConfig);

    } else {
      client = ClientBuilder.newBuilder()//
          .sslContext(sslContext)//
          .withConfig(clientConfig)//
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

  public RestResult<AboutInfo> getAbout() {
    WebTarget path = rootTarget//
        .path(ResourcePaths.ABOUT);
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<AboutInfo> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toAboutInfoRestResult)//
        .getResult();
  }

  public RestResult<Void> setDocument(UUID documentUUID, String lmnl) {
    final WebTarget path = documentTarget(documentUUID);
    final Entity<String> entity = Entity.entity(lmnl, MediaType.TEXT_PLAIN);
    final Supplier<Response> responseSupplier = anonymousPut(path, entity);
    final RestRequester<Void> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, voidRestResult())//
        .onStatus(Status.NO_CONTENT, voidRestResult())//
        .getResult();
  }

  public RestResult<UUID> addDocument(String lmnl) {
    final WebTarget path = documentsTarget();
    final Entity<String> entity = Entity.entity(lmnl, UTF8MediaType.TEXT_PLAIN);
    final Supplier<Response> responseSupplier = anonymousPost(path, entity);
    final RestRequester<UUID> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.CREATED, this::uuidFromLocationHeader)//
        .getResult();
  }

  public RestResult<String> getLMNL(UUID documentUUID) {
    WebTarget path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_LMNL);
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

  // private methods
  private WebTarget documentTarget(UUID documentUUID) {
    return documentsTarget()//
        .path(documentUUID.toString());
  }

  private WebTarget documentsTarget() {
    return rootTarget//
        .path(ResourcePaths.DOCUMENTS);
  }

  private RestResult<String> toStringRestResult(final Response response) {
    return toEntityRestResult(response, String.class);
  }

  private RestResult<AboutInfo> toAboutInfoRestResult(final Response response) {
    return toEntityRestResult(response, AboutInfo.class);
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
    return () -> target.request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .put(entity);
  }

  private Supplier<Response> authorizedPut(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).put(entity);
  }

  private Supplier<Response> anonymousPost(final WebTarget target, final Entity<?> entity) {
    return () -> target.request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .post(entity);
  }

  private Supplier<Response> authorizedPost(final WebTarget path, final Entity<?> entity) {
    return () -> authorizedRequest(path).post(entity);
  }

  private Supplier<Response> authorizedDelete(final WebTarget path) {
    return () -> authorizedRequest(path).delete();
  }

  private SyncInvoker authorizedRequest(final WebTarget target) {
    return target.request()//
        .accept(MediaType.APPLICATION_JSON_TYPE)//
        .header(HEADER_AUTH, authHeader);
  }

  private RestResult<String> stringResult(WebTarget path) {
    Supplier<Response> responseSupplier = anonymousGet(path);
    final RestRequester<String> requester = RestRequester.withResponseSupplier(responseSupplier);
    return requester//
        .onStatus(Status.OK, this::toStringRestResult)//
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
    return rootTarget//
        .path(ResourcePaths.DOCUMENTS)//
    ;
  }

  public WebTarget getRootTarget() {
    return rootTarget;
  }
}
