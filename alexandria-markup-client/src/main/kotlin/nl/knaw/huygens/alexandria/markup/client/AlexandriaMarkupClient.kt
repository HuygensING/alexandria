package nl.knaw.huygens.alexandria.markup.client


/*
* #%L
 * alexandria-markup-client
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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider
import nl.knaw.huygens.alexandria.markup.api.AppInfo
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths
import nl.knaw.huygens.alexandria.markup.api.UTF8MediaType
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.glassfish.jersey.apache.connector.ApacheClientProperties
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider
import org.glassfish.jersey.client.ClientConfig
import org.glassfish.jersey.client.ClientProperties
import java.net.URI
import java.util.*
import java.util.function.Supplier
import javax.net.ssl.SSLContext
import javax.ws.rs.client.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

class AlexandriaMarkupClient @JvmOverloads constructor(private val alexandriaMarkupURI: URI, sslContext: SSLContext? = null) : AutoCloseable {
    var rootTarget: WebTarget
        private set
    private var client: Client? = null
    override fun close() {
        client!!.close()
    }

    fun register(component: Any?) {
        client!!.register(component)
        rootTarget = client!!.target(alexandriaMarkupURI)
    }

    fun setProperty(jerseyClientProperty: String?, value: Any?) {
        client!!.property(jerseyClientProperty, value)
        rootTarget = client!!.target(alexandriaMarkupURI)
    }

    // Alexandria Markup API methods
    val about: RestResult<AppInfo>
        get() {
            val path = rootTarget.path(ResourcePaths.ABOUT)
            val responseSupplier = anonymousGet(path)
            val requester: RestRequester<AppInfo> = RestRequester.Companion.withResponseSupplier<AppInfo>(responseSupplier)
            return requester.onStatus(Response.Status.OK) { response: Response -> toAboutInfoRestResult(response) }.result
        }

    fun setDocumentFromTAGML(documentUUID: UUID, tagml: String): RestResult<Void> {
        val path = documentTarget(documentUUID).path("tagml")
        return setDocument(tagml, path)
    }

    fun setDocumentFromTexMECS(documentUUID: UUID, texMECS: String): RestResult<Void> {
        val path = documentTarget(documentUUID).path("tagml")
        return setDocument(texMECS, path)
    }

    private fun setDocument(serializedDocument: String, path: WebTarget): RestResult<Void> {
        val entity = Entity.entity(serializedDocument, MediaType.TEXT_PLAIN)
        val responseSupplier = anonymousPut(path, entity)
        val requester: RestRequester<Void> = RestRequester.Companion.withResponseSupplier<Void>(responseSupplier)
        return requester
                .onStatus(Response.Status.CREATED, voidRestResult())
                .onStatus(Response.Status.NO_CONTENT, voidRestResult())
                .result
    }

    fun addDocumentFromTAGML(tagml: String): RestResult<UUID> {
        val path = documentsTarget().path("tagml")
        return addDocument(tagml, path)
    }

    fun addDocumentFromTexMECS(texMECS: String): RestResult<UUID> {
        val path = documentsTarget().path("texmecs")
        return addDocument(texMECS, path)
    }

    private fun addDocument(serializedDocument: String, path: WebTarget): RestResult<UUID> {
        val entity = Entity.entity(serializedDocument, UTF8MediaType.TEXT_PLAIN)
        val responseSupplier = anonymousPost(path, entity)
        val requester: RestRequester<UUID> = RestRequester.Companion.withResponseSupplier<UUID>(responseSupplier)
        return requester
                .onStatus(Response.Status.CREATED) { response: Response -> uuidFromLocationHeader(response) }
                .result
    }

    fun getTAGML(documentUUID: UUID): RestResult<String> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_TAGML)
        return stringResult(path)
    }

    fun getDocumentLaTeX(documentUUID: UUID): RestResult<String> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_LATEX)
        return stringResult(path)
    }

    fun getMarkupDepthLaTex(documentUUID: UUID): RestResult<String> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_MARKUPDEPTH)
        return stringResult(path)
    }

    fun getMatrixLaTex(documentUUID: UUID): RestResult<String> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_MATRIX)
        return stringResult(path)
    }

    fun getKdTreeLaTex(documentUUID: UUID): RestResult<String> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_KDTREE)
        return stringResult(path)
    }

    fun postTAGQLQuery(documentUUID: UUID, query: String): RestResult<JsonNode> {
        val path = documentTarget(documentUUID).path(ResourcePaths.DOCUMENTS_QUERY)
        val entity = Entity.entity(query, UTF8MediaType.TEXT_PLAIN)
        val responseSupplier = anonymousPost(path, entity)
        val requester: RestRequester<JsonNode> = RestRequester.Companion.withResponseSupplier<JsonNode>(responseSupplier)
        return requester
                .onStatus(Response.Status.OK) { response: Response -> toJsonObjectRestResult(response) }
                .result
    }

    // private methods
    private fun documentTarget(documentUUID: UUID): WebTarget {
        return documentsTarget().path(documentUUID.toString())
    }

    private fun documentsTarget(): WebTarget {
        return rootTarget.path(ResourcePaths.DOCUMENTS)
    }

    private fun toStringRestResult(response: Response): RestResult<String> {
        return toEntityRestResult<String>(response, String::class.java)
    }

    private fun toAboutInfoRestResult(response: Response): RestResult<AppInfo> {
        return toEntityRestResult<AppInfo>(response, AppInfo::class.java)
    }

    private fun toJsonObjectRestResult(response: Response): RestResult<JsonNode> {
        return toEntityRestResult<JsonNode>(response, JsonNode::class.java)
    }

    private fun <E> toEntityRestResult(
            response: Response,
            entityClass: Class<E>
    ): RestResult<E> {
        val result = RestResult<E>()
        val cargo = response.readEntity(entityClass)
        return result.withCargo(cargo)
    }

    private fun uriFromLocationHeader(response: Response): RestResult<URI> {
        val result = RestResult<URI>()
        val location = response.getHeaderString(LOCATION)
        val uri = URI.create(location)
        result.withCargo(uri)
        return result
    }

    private fun uuidFromLocationHeader(response: Response): RestResult<UUID> {
        val result = RestResult<UUID>()
        val location = response.getHeaderString(LOCATION)
        val uuid = UUID.fromString(location.replaceFirst(".*/".toRegex(), ""))
        return result.withCargo(uuid)
    }

    private fun anonymousGet(target: WebTarget): Supplier<Response> {
        return Supplier { target.request().get() }
    }

    private fun anonymousPut(target: WebTarget, entity: Entity<*>): Supplier<Response> {
        return Supplier { target.request().accept(MediaType.APPLICATION_JSON_TYPE).put(entity) }
    }

    private fun authorizedPut(path: WebTarget, entity: Entity<*>): Supplier<Response> {
        return Supplier { authorizedRequest(path).put(entity) }
    }

    private fun anonymousPost(target: WebTarget, entity: Entity<*>): Supplier<Response> {
        return Supplier { target.request().accept(MediaType.APPLICATION_JSON_TYPE).post(entity) }
    }

    private fun authorizedPost(path: WebTarget, entity: Entity<*>): Supplier<Response> {
        return Supplier { authorizedRequest(path).post(entity) }
    }

    private fun authorizedDelete(path: WebTarget): Supplier<Response> {
        return Supplier { authorizedRequest(path).delete() }
    }

    private fun authorizedRequest(target: WebTarget): SyncInvoker {
        val authHeader = ""
        return target.request().accept(MediaType.APPLICATION_JSON_TYPE).header(HEADER_AUTH, authHeader)
    }

    private fun stringResult(path: WebTarget): RestResult<String> {
        val responseSupplier = anonymousGet(path)
        val requester: RestRequester<String> = RestRequester.Companion.withResponseSupplier<String>(responseSupplier)
        return requester.onStatus(Response.Status.OK) { response: Response -> toStringRestResult(response) }.result
    }

    private fun voidRestResult(): ResponseMapper<Void> =
            { response: Response ->
                response.bufferEntity() // to notify connectors, such as the ApacheConnector, that the entity has
                RestResult()
            }

    private val documentTarget: WebTarget
        get() = rootTarget.path(ResourcePaths.DOCUMENTS)

    companion object {
        const val LOCATION = "Location"
        private const val HEADER_AUTH = "auth"
    }

    // private boolean autoConfirm = true;
    init {
        val objectMapper = ObjectMapper().apply { findAndRegisterModules() }
        val jacksonProvider = JacksonJaxbJsonProvider().apply { setMapper(objectMapper) }
        val cm = PoolingHttpClientConnectionManager().apply {
            maxTotal = 50
            defaultMaxPerRoute = 50
        }
        val connectorProvider = ApacheConnectorProvider()
        val clientConfig = ClientConfig(jacksonProvider)
                .connectorProvider(connectorProvider)
                .property(ApacheClientProperties.CONNECTION_MANAGER, cm)
                .property(ClientProperties.CONNECT_TIMEOUT, 60000)
                .property(ClientProperties.READ_TIMEOUT, 60000)
        client = if (sslContext == null) {
            if ("https" == alexandriaMarkupURI.scheme) {
                throw RuntimeException(
                        "SSL connections need an SSLContext, use: new AlexandriaClient(uri, sslContext) instead.")
            }
            ClientBuilder.newClient(clientConfig)
        } else {
            ClientBuilder.newBuilder().sslContext(sslContext).withConfig(clientConfig).build()
        }
        rootTarget = client!!.target(alexandriaMarkupURI)
    }
}
