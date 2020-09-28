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
import nl.knaw.huygens.alexandria.markup.api.AppInfo
import java.net.URI
import java.util.*
import javax.net.ssl.SSLContext
import javax.ws.rs.client.WebTarget
import javax.ws.rs.core.Response

class OptimisticAlexandriaMarkupClient {
    private val delegate: AlexandriaMarkupClient

    // constructors
    constructor(alexandriaURI: URI) {
        delegate = AlexandriaMarkupClient(alexandriaURI)
    }

    constructor(alexandriaURI: String?) : this(URI.create(alexandriaURI)) {}
    constructor(alexandriaURI: URI, sslContext: SSLContext?) {
        delegate = AlexandriaMarkupClient(alexandriaURI, sslContext)
    }

    constructor(alexandriaURI: String?, sslContext: SSLContext?) : this(URI.create(alexandriaURI), sslContext) {}

    // convenience methods
    // delegated methods
    val rootTarget: WebTarget
        get() = delegate.rootTarget

    fun close() {
        delegate.close()
    }

    fun setProperty(jerseyClientProperty: String?, value: Any?) {
        delegate.setProperty(jerseyClientProperty, value)
    }

    val about: AppInfo
        get() = unwrap(delegate.about)

    fun getTAGML(uuid: UUID): String = unwrap(delegate.getTAGML(uuid))

    fun getMarkupDepthLaTex(uuid: UUID): String = unwrap(delegate.getMarkupDepthLaTex(uuid))

    fun getDocumentLaTeX(uuid: UUID): String = unwrap(delegate.getDocumentLaTeX(uuid))

    fun getMatrixLaTex(uuid: UUID): String = unwrap(delegate.getMatrixLaTex(uuid))

    fun getKdTreeLaTex(uuid: UUID): String = unwrap(delegate.getKdTreeLaTex(uuid))

    fun postTAGQLQuery(uuid: UUID, string: String): JsonNode = unwrap(delegate.postTAGQLQuery(uuid, string))

    fun addDocumentFromTexMECS(string: String): UUID = unwrap(delegate.addDocumentFromTexMECS(string))

    fun setDocumentFromTexMECS(uuid: UUID, string: String) {
        unwrap(delegate.setDocumentFromTexMECS(uuid, string))
    }

    fun addDocumentFromTAGML(string: String): UUID = unwrap(delegate.addDocumentFromTAGML(string))

    fun setDocumentFromTAGML(uuid: UUID, string: String) {
        unwrap(delegate.setDocumentFromTAGML(uuid, string))
    }

    /////// end delegated methods
    private fun <T> unwrap(restResult: RestResult<T>): T {
        if (restResult.hasFailed()) {
            val response = restResult.getResponse()
            val status = response
                    .map { response1: Response -> response1.status.toString() + ": " }
                    .orElse("")
            val message = status + restResult.failureCause.orElse("Unspecified error")
            throw AlexandriaException(message)
        }
        return restResult.get()
    }
}
