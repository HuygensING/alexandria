package nl.knaw.huygens.alexandria.dropwizard.api


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

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration
import nl.knaw.huygens.alexandria.markup.api.DocumentInfo
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import nl.knaw.huygens.alexandria.storage.dto.TAGDocumentDTO
import java.time.Instant
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class DocumentService(config: ServerConfiguration) {
    private val uuids: MutableSet<UUID> = LinkedHashSet()
    private val store: TAGStore

    fun getDocument(uuid: UUID): Optional<TAGDocument> {
        if (uuids.contains(uuid)) {
            try {
                val document = TAGDocument(store, documentCache[uuid, readDocument(uuid)])
                return Optional.of(document)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return Optional.empty()
    }

    fun setDocument(docId: UUID, document: TAGDocument) {
        documentCache.put(docId, document.dto)
        val docInfo = getDocumentInfo(docId).orElseGet { newDocumentInfo(docId) }
        docInfo!!.modified = Instant.now()
        documentInfoCache.put(docId, docInfo)
        uuids.add(docId)
    }

    fun getDocumentInfo(uuid: UUID): Optional<DocumentInfo> {
        if (uuids.contains(uuid)) {
            try {
                val documentInfo = documentInfoCache[uuid, { null }]
                return Optional.ofNullable(documentInfo)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return Optional.empty()
    }

    val documentUUIDs: Collection<UUID>
        get() = uuids

    companion object {
        private var baseURI: String? = null
        val documentCache: Cache<UUID, TAGDocumentDTO> = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build()

        private fun readDocument(uuid: UUID): Callable<out TAGDocumentDTO> =
                Callable<TAGDocumentDTO> { null }

        val documentInfoCache: Cache<UUID, DocumentInfo> = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build()

        private fun readDocumentInfo(uuid: UUID): Callable<out DocumentInfo> {
            return Callable { newDocumentInfo(uuid) }
        }

        private fun newDocumentInfo(uuid: UUID): DocumentInfo =
                DocumentInfo(uuid, baseURI!!)
                        .withCreated(Instant.now())
                        .withModified(Instant.now())
    }

    init {
        baseURI = config.baseURI
        store = config.store!!
    }
}
