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
import nl.knaw.huygens.alexandria.storage.TAGDocument
import nl.knaw.huygens.alexandria.storage.TAGStore
import java.util.*

class NamedDocumentService(private val store: TAGStore) {

    fun registerDocument(document: TAGDocument, docName: String) {
        documentIdCache.put(docName, document.dbId)
    }

    fun getDocumentByName(docName: String): Optional<TAGDocument> {
        val docId = documentIdCache.getIfPresent(docName)
        return if (docId == null) {
            Optional.empty()
        } else {
            val document = store.getDocument(docId) ?: return Optional.empty()
            Optional.of(document)
        }
    }

    companion object {
        val documentIdCache: Cache<String, Long> = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build()
    }
}
