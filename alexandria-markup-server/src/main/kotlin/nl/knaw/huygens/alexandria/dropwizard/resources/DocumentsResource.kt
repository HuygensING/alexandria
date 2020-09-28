package nl.knaw.huygens.alexandria.dropwizard.resources

/*
* #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import com.codahale.metrics.annotation.Timed
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import nl.knaw.huc.di.tag.tagml.TAGMLSyntaxError
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService
import nl.knaw.huygens.alexandria.exporter.LaTeXExporter
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType
import nl.knaw.huygens.alexandria.query.TAGQLQueryHandler
import nl.knaw.huygens.alexandria.query.TAGQLResult
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSSyntaxError
import java.net.URI
import java.util.*
import java.util.stream.Collectors
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Api(ResourcePaths.DOCUMENTS)
@Path(ResourcePaths.DOCUMENTS)
@Produces(MediaType.APPLICATION_JSON)
class DocumentsResource(
        private val documentService: DocumentService,
        private val tagmlImporter: TAGMLImporter,
        private val texMECSImporter: TexMECSImporter,
        private val tagmlExporter: TAGMLExporter,
        private val configuration: ServerConfiguration) {
    private val store: TAGStore? = configuration.store

    @get:ApiOperation(value = "List all document URIs")
    @get:Timed
    @get:GET
    val documentURIs: List<URI>
        get() = documentService.documentUUIDs.stream()
                .map { documentId: UUID -> documentURI(documentId) }
                .collect(Collectors.toList())

    @POST
    @Consumes(UTF8MediaType.TEXT_PLAIN)
    @Path("tagml")
    @Timed
    @ApiOperation(value = "Create a new document from a TAGML text")
    fun addDocumentFromTAGML(
            @ApiParam(APIPARAM_TAGML) @NotNull @Valid tagml: String
    ): Response {
        val documentId = UUID.randomUUID()
        return try {
            processAndStoreTAGML(tagml, documentId)
            Response.created(documentURI(documentId)).build()
        } catch (e: TAGMLSyntaxError) {
            e.printStackTrace()
            throw BadRequestException(e.message)
        }
    }

    @POST
    @Consumes(UTF8MediaType.TEXT_PLAIN)
    @Path("texmecs")
    @Timed
    @ApiOperation(value = "Create a new document from a TexMECS text")
    fun addDocumentFromTexMECS(
            @ApiParam(APIPARAM_TEXMECS) @NotNull @Valid texmecs: String
    ): Response {
        val documentId = UUID.randomUUID()
        return try {
            processAndStoreTexMECS(texmecs, documentId)
            Response.created(documentURI(documentId)).build()
        } catch (se: TexMECSSyntaxError) {
            se.printStackTrace()
            throw BadRequestException(se.message)
        }
    }

    @PUT
    @Consumes(UTF8MediaType.TEXT_PLAIN)
    @Path("{uuid}/tagml")
    @Timed
    @ApiOperation(value = "Update an existing document from a TAGML text")
    fun setDocumentFromTAGML(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID,
            @ApiParam(APIPARAM_TAGML) @NotNull tagml: String
    ): Response =
            try {
                processAndStoreTAGML(tagml, uuid)
                Response.noContent().build()
            } catch (se: TAGMLSyntaxError) {
                throw BadRequestException(se.message)
            }

    @PUT
    @Consumes(UTF8MediaType.TEXT_PLAIN)
    @Path("{uuid}/texmecs")
    @Timed
    @ApiOperation(value = "Update an existing document from a TexMECS text")
    fun setDocumentFromTexMECS(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID,
            @ApiParam(APIPARAM_TEXMECS) @NotNull texMECS: String
    ): Response =
            try {
                processAndStoreTexMECS(texMECS, uuid)
                Response.noContent().build()
            } catch (se: TexMECSSyntaxError) {
                throw BadRequestException(se.message)
            }

    @GET
    @Path("{uuid}")
    @Timed
    @ApiOperation(value = "Get info about a document")
    fun getDocumentInfo(@ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID): Response {
        val documentInfo = documentService.getDocumentInfo(uuid)
                .orElseThrow { NotFoundException() }
        return Response.ok(documentInfo).build()
    }

    @GET
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_TAGML)
    @Timed
    @Produces(UTF8MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a TAGML representation of the document")
    fun getTAGML(@ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID): Response {
        val document = getExistingDocument(uuid)
        val tagml = tagmlExporter.asTAGML(document)
        return Response.ok(tagml).build()
    }

    @GET
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_LATEX)
    @Timed
    @Produces(UTF8MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a LaTeX visualization of the main layer of a document as text nodes and markup nodes")
    fun getLaTeXVisualization(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID
    ): Response {
        val document = getExistingDocument(uuid)
        val latexExporter = LaTeXExporter(store, document)
        val latex = latexExporter.exportDocument()
        return Response.ok(latex).build()
    }

    @GET
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MARKUPDEPTH)
    @Timed
    @Produces(UTF8MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a LaTeX visualization of the main text nodes of a document, color-coded for the number of different markup nodes per text node")
    fun getRangeOverlapVisualization(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID
    ): Response {
        val document = getExistingDocument(uuid)
        val latexExporter = LaTeXExporter(store, document)
        val latex = latexExporter.exportMarkupOverlap()
        return Response.ok(latex).build()
    }

    @GET
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_MATRIX)
    @Timed
    @Produces(UTF8MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a LaTeX visualization of the optimized text node / markup matrix of a document")
    fun getMatrixVisualization(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID
    ): Response {
        val document = getExistingDocument(uuid)
        val latexExporter = LaTeXExporter(store, document)
        val latex = latexExporter.exportMatrix()
        return Response.ok(latex).build()
    }

    @GET
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_KDTREE)
    @Timed
    @Produces(UTF8MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get a LaTeX visualization of the kd-tree of a document")
    fun getKdTreeVisualization(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID
    ): Response {
        val document = getExistingDocument(uuid)
        val latexExporter = LaTeXExporter(store, document)
        val latex = latexExporter.exportKdTree()
        return Response.ok(latex).build()
    }

    @POST
    @Path("{uuid}/" + ResourcePaths.DOCUMENTS_QUERY)
    @Timed
    @Produces(UTF8MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Run a TAGQL query on a document", response = TAGQLResult::class)
    fun postTAGQLQuery(
            @ApiParam(APIPARAM_UUID) @PathParam("uuid") uuid: UUID,
            @ApiParam("TAGQL query") tagqlQuery: String
    ): Response {
        val document = getExistingDocument(uuid)
        val h = TAGQLQueryHandler(document!!)
        val result = h.execute(tagqlQuery)
        return Response.ok(result).build()
    }

    private fun documentURI(documentId: UUID): URI =
            URI.create(configuration.baseURI + "/documents/" + documentId)

    @Throws(TAGMLSyntaxError::class)
    private fun processAndStoreTAGML(tagml: String, documentId: UUID) {
        val document = tagmlImporter.importTAGML(tagml)
        documentService.setDocument(documentId, document)
    }

    @Throws(TexMECSSyntaxError::class)
    private fun processAndStoreTexMECS(texMECS: String, documentId: UUID) {
        val document = texMECSImporter.importTexMECS(texMECS)
        documentService.setDocument(documentId, document)
    }

    private fun getExistingDocument(uuid: UUID): TAGDocument? =
            documentService.getDocument(uuid)
                    .orElseThrow { NotFoundException() }

    companion object {
        const val APIPARAM_UUID = "document UUID"
        const val APIPARAM_TEXMECS = "TexMECS text"
        const val APIPARAM_TAGML = "TAGML text"
    }

}
