package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentInfo;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.lmnl.data_model.Document;
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.lmnl.exporter.LaTeXExporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;

@Path(RootPaths.DOCUMENTS)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentsResource {

  public static class SubPaths {
    public static final String LMNL = "lmnl";
    public static final String LATEX = "latex";
    public static final String MARKUPDEPTH = "markupdepth";
    public static final String MATRIX = "matrix";
    public static final String KDTREE = "kdtree";
  }

  final DocumentService documentService;
  private LMNLImporter lmnlImporter;
  private LMNLExporter lmnlExporter;
  private ServerConfiguration configuration;

  public DocumentsResource(DocumentService documentService, LMNLImporter lmnlImporter, LMNLExporter lmnlExporter, ServerConfiguration configuration) {
    this.documentService = documentService;
    this.lmnlImporter = lmnlImporter;
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
  @Consumes(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  @Timed
  public Response addDocument(@NotNull @Valid String lmnl) {
    UUID documentId = UUID.randomUUID();
    processAndStore(lmnl, documentId);
    return Response.created(documentURI(documentId)).build();
  }

  private URI documentURI(UUID documentId) {
    return URI.create(configuration.getBaseURI() + "/documents/" + documentId);
  }

  private void processAndStore(String lmnl, UUID documentId) {
    Document document = lmnlImporter.importLMNL(lmnl);
    documentService.setDocument(documentId, document);
  }

  @PUT
  @Consumes(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  @Path("{uuid}")
  @Timed
  public Response setDocument(@PathParam("uuid") final UUID uuid, @NotNull @Valid String lmnl) {
    processAndStore(lmnl, uuid);
    return Response.created(documentURI(uuid)).build();
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
  @Path("{uuid}/" + SubPaths.LMNL)
  @Timed
  @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  public Response getLMNL(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    String lmnl = lmnlExporter.toLMNL(document);
    return Response.ok(lmnl).build();
  }

  @GET
  @Path("{uuid}/" + SubPaths.LATEX)
  @Timed
  @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  public Response getLaTeXVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportDocument();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + SubPaths.MARKUPDEPTH)
  @Timed
  @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  public Response getRangeOverlapVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportTextRangeOverlap();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + SubPaths.MATRIX)
  @Timed
  @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  public Response getMatrixVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportMatrix();
    return Response.ok(latex).build();
  }

  @GET
  @Path("{uuid}/" + SubPaths.KDTREE)
  @Timed
  @Produces(MediaType.TEXT_PLAIN + ";charset=UTF-8")
  public Response getKdTreeVisualization(@PathParam("uuid") final UUID uuid) {
    Document document = getExistingDocument(uuid);
    LaTeXExporter latexExporter = new LaTeXExporter(document);
    String latex = latexExporter.exportKdTree();
    return Response.ok(latex).build();
  }

  private Document getExistingDocument(final UUID uuid) {
    return documentService.getDocument(uuid)//
        .orElseThrow(NotFoundException::new);
  }

}
