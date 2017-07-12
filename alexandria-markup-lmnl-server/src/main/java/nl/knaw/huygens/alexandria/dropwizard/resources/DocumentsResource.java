package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.lmnl.data_model.Document;
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.lmnl.exporter.LaTeXExporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLSyntaxError;
import nl.knaw.huygens.alexandria.lmnl.query.TAGQLQueryHandler;
import nl.knaw.huygens.alexandria.lmnl.query.TAGQLResult;
import nl.knaw.huygens.alexandria.markup.api.DocumentInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSSyntaxError;

/*
 * #%L
 * alexandria-markup-lmnl-server
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
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

@Path(ResourcePaths.DOCUMENTS)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

  private final DocumentService documentService;
  private final LMNLImporter lmnlImporter;
  private final LMNLExporter lmnlExporter;
  private final ServerConfiguration configuration;
  private final TexMECSImporter texMECSImporter;

  public DocumentsResource(DocumentService documentService, LMNLImporter lmnlImporter, TexMECSImporter texMECSImporter, LMNLExporter lmnlExporter, ServerConfiguration configuration) {
    this.documentService = documentService;
    this.lmnlImporter = lmnlImporter;
    this.texMECSImporter = texMECSImporter;
    this.lmnlExporter = lmnlExporter;
    this.configuration = configuration;
  }

  @GET
  @Timed
  public List<URI> getDocumentURIs() {
    return documentService.getDocumentUUIDs()//
        .stream()//
        .map(this::documentURI)//
        .collect(Collectors.toList());
  }

  @POST
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("lmnl")
  @Timed
  public Response addDocumentFromLMNL(@NotNull @Valid String lmnl) {
    UUID documentId = UUID.randomUUID();
    try {
      processAndStoreLMNL(lmnl, documentId);
      return Response.created(documentURI(documentId)).build();

    } catch (LMNLSyntaxError e) {
      e.printStackTrace();
      throw new BadRequestException(e.getMessage());
    }
  }

  @POST
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("texmecs")
  @Timed
  public Response addDocumentFromTexMECS(@NotNull @Valid String texmecs) {
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
  @Path("{uuid}/lmnl")
  @Timed
  public Response setDocumentFromLMNL(@PathParam("uuid") final UUID uuid, @NotNull String lmnl) {
    try {
      processAndStoreLMNL(lmnl, uuid);
      return Response.noContent().build();
    } catch (LMNLSyntaxError se) {
      throw new BadRequestException(se.getMessage());
    }
  }

  @PUT
  @Consumes(UTF8MediaType.TEXT_PLAIN)
  @Path("{uuid}/texmecs")
  @Timed
  public Response setDocumentFromTexMECS(@PathParam("uuid") final UUID uuid, @NotNull String texMECS) {
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
  public Response getDocumentInfo(@PathParam("uuid") final UUID uuid) {
    DocumentInfo documentInfo = documentService.getDocumentInfo(uuid)//
        .orElseThrow(NotFoundException::new);
    return Response.ok(documentInfo).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_LMNL)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  public Response getLMNL(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    String lmnl = lmnlExporter.toLMNL(document);
    return Response.ok(lmnl).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_LATEX)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  public Response getLaTeXVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportDocument();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MARKUPDEPTH)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  public Response getRangeOverlapVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportMarkupOverlap();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MATRIX)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  public Response getMatrixVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportMatrix();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_KDTREE)
  @Timed
  @Produces(UTF8MediaType.TEXT_PLAIN)
  public Response getKdTreeVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportKdTree();
    return Response.ok(latex).build();
  }

  @POST
  @Path("{uuid}/" + ResourcePaths.DOCUMENTS_QUERY)
  @Timed
  @Produces(UTF8MediaType.APPLICATION_JSON)
  public Response postTAGQLQuery(@PathParam("uuid") final UUID uuid, String tagqlQuery) {
    Document document = getExistingDocument(uuid);
    TAGQLQueryHandler h = new TAGQLQueryHandler(document);
    TAGQLResult result = h.execute(tagqlQuery);
    return Response.ok(result).build();
  }

  private URI documentURI(UUID documentId) {
    return URI.create(configuration.getBaseURI() + "/documents/" + documentId);
  }

  private void processAndStoreLMNL(String lmnl, UUID documentId) throws LMNLSyntaxError {
    Document document = lmnlImporter.importLMNL(lmnl);
    documentService.setDocument(documentId, document);
  }

  private void processAndStoreTexMECS(String texMECS, UUID documentId) throws TexMECSSyntaxError {
    Document document = texMECSImporter.importTexMECS(texMECS);
    documentService.setDocument(documentId, document);
  }

  private Document getExistingDocument(final UUID uuid) {
    return documentService.getDocument(uuid)//
        .orElseThrow(NotFoundException::new);
  }

}
