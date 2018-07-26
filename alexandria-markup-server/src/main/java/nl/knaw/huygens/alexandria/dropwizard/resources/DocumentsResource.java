package nl.knaw.huygens.alexandria.dropwizard.resources;

/*
 * #%L
 * alexandria-markup-lmnl-server
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

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError;
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.exporter.LaTeXExporter;
import nl.knaw.huygens.alexandria.markup.api.DocumentInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType;
import nl.knaw.huygens.alexandria.query.TAGQLQueryHandler;
import nl.knaw.huygens.alexandria.query.TAGQLResult;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSSyntaxError;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Api(ResourcePaths.DOCUMENTS)
@Path(ResourcePaths.DOCUMENTS)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

  public static final String APIPARAM_UUID = "document UUID";
  public static final String APIPARAM_TEXMECS = "TexMECS text";
  public static final String APIPARAM_TAGML = "TAGML text";

  private final DocumentService documentService;
  private final TAGMLImporter tagmlImporter;
  private final TAGMLExporter tagmlExporter;
  private final ServerConfiguration configuration;
  private final TexMECSImporter texMECSImporter;
  private final TAGStore store;

  public DocumentsResource(DocumentService documentService, TAGMLImporter tagmlImporter, TexMECSImporter texMECSImporter, TAGMLExporter tagmlExporter, ServerConfiguration configuration) {
    this.documentService = documentService;
    this.tagmlImporter = tagmlImporter;
    this.texMECSImporter = texMECSImporter;
    this.tagmlExporter = tagmlExporter;
    this.configuration = configuration;
    this.store = configuration.getStore();
  }

  @GET
  @Timed
  @ApiOperation(value = "List all document URIs")
  public List<URI> getDocumentURIs() {
    return documentService.getDocumentUUIDs()//
        .stream()//
        .map(this::documentURI)//
        .collect(Collectors.toList());
  }

  @POST
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("tagml")
  @Timed
  @ApiOperation(value = "Create a new document from a TAGML text")
  public Response addDocumentFromTAGML(
      @ApiParam(APIPARAM_TAGML) @NotNull @Valid String tagml) {
    UUID documentId = UUID.randomUUID();
    try {
      processAndStoreTAGML(tagml, documentId);
      return Response.created(documentURI(documentId)).build();

    } catch (TAGMLSyntaxError e) {
      e.printStackTrace();
      throw new BadRequestException(e.getMessage());
    }
  }

  @POST
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("texmecs")
  @Timed
  @ApiOperation(value = "Create a new document from a TexMECS text")
  public Response addDocumentFromTexMECS(
      @ApiParam(APIPARAM_TEXMECS) @NotNull @Valid String texmecs) {
    UUID documentId = UUID.randomUUID();
    try {
      processAndStoreTexMECS(texmecs, documentId);
      return Response.created(documentURI(documentId)).build();

    } catch (TexMECSSyntaxError se) {
      se.printStackTrace();
      throw new BadRequestException(se.getMessage());
    }
  }

  @PUT
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("{uuid}/tagml")
  @Timed
  @ApiOperation(value = "Update an existing document from a TAGML text")
  public Response setDocumentFromTAGML(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid,//
      @ApiParam(APIPARAM_TAGML) @NotNull String tagml) {
    try {
      processAndStoreTAGML(tagml, uuid);
      return Response.noContent().build();
    } catch (TAGMLSyntaxError se) {
      throw new BadRequestException(se.getMessage());
    }
  }

  @PUT
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("{uuid}/texmecs")
  @Timed
  @ApiOperation(value = "Update an existing document from a TexMECS text")
  public Response setDocumentFromTexMECS(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid,//
      @ApiParam(APIPARAM_TEXMECS) @NotNull String texMECS) {
    try {
      processAndStoreTexMECS(texMECS, uuid);
      return Response.noContent().build();

    } catch (TexMECSSyntaxError se) {
      throw new BadRequestException(se.getMessage());
    }
  }

  @GET
  @Path("{uuid}")
  @Timed
  @ApiOperation(value = "Get info about a document")
  public Response getDocumentInfo(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    DocumentInfo documentInfo = documentService.getDocumentInfo(uuid)//
        .orElseThrow(NotFoundException::new);
    return Response.ok(documentInfo).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_TAGML)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get a TAGML representation of the document")
  public Response getTAGML(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    TAGDocument document = getExistingDocument(uuid);
    String tagml = tagmlExporter.asTAGML(document);
    return Response.ok(tagml).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_LATEX)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get a LaTeX visualization of the main layer of a document as text nodes and markup nodes")
  public Response getLaTeXVisualization(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    TAGDocument document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(store, document);
    String latex = latexExporter.exportDocument();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MARKUPDEPTH)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get a LaTeX visualization of the main text nodes of a document, color-coded for the number of different markup nodes per text node")
  public Response getRangeOverlapVisualization(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    TAGDocument document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(store, document);
    String latex = latexExporter.exportMarkupOverlap();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MATRIX)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get a LaTeX visualization of the optimized text node / markup matrix of a document")
  public Response getMatrixVisualization(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    TAGDocument document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(store, document);
    String latex = latexExporter.exportMatrix();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_KDTREE)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  @ApiOperation(value = "Get a LaTeX visualization of the kd-tree of a document")
  public Response getKdTreeVisualization(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid) {
    TAGDocument document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(store, document);
    String latex = latexExporter.exportKdTree();
    return Response.ok(latex).build();
  }

  @POST
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_QUERY)
  @Timed
  @Produces(UTF8MediaType.APPLICATION_JSON)
  @ApiOperation(value = "Run a TAGQL query on a document", response = TAGQLResult.class)
  public Response postTAGQLQuery(
      @ApiParam(APIPARAM_UUID) @PathParam("uuid") final UUID uuid,//
      @ApiParam("TAGQL query") String tagqlQuery) {
    TAGDocument document = getExistingDocument(uuid);
    TAGQLQueryHandler h = new TAGQLQueryHandler(document);
    TAGQLResult result = h.execute(tagqlQuery);
    return Response.ok(result).build();
  }

  private URI documentURI(UUID documentId) {
    return URI.create(configuration.getBaseURI() + "/documents/" + documentId);
  }

  private void processAndStoreTAGML(String tagml, UUID documentId) throws TAGMLSyntaxError {
    TAGDocument document = tagmlImporter.importTAGML(tagml);
    documentService.setDocument(documentId, document);
  }

  private void processAndStoreTexMECS(String texMECS, UUID documentId) throws TexMECSSyntaxError {
    TAGDocument document = texMECSImporter.importTexMECS(texMECS);
    documentService.setDocument(documentId, document);
  }

  private TAGDocument getExistingDocument(final UUID uuid) {
    return documentService.getDocument(uuid)//
        .orElseThrow(NotFoundException::new);
  }

}
