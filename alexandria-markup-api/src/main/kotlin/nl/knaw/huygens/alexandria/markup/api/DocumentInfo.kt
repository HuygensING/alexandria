package nl.knaw.huygens.alexandria.markup.api

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.time.Instant
import java.util.*

/*
* #%L
 * alexandria-markup-api
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

class DocumentInfo(documentId: UUID, baseURL: String) {
    var created: Instant? = null
    var modified: Instant? = null

    private val uriBase: String by lazy { "$baseURL/${ResourcePaths.DOCUMENTS}/$documentId/" }

    fun withCreated(created: Instant): DocumentInfo {
        this.created = created
        return this
    }

    fun getCreated(): String {
        return created.toString()
    }

    fun withModified(modified: Instant): DocumentInfo {
        this.modified = modified
        return this
    }

    fun getModified(): String {
        return modified.toString()
    }

    @get:JsonProperty("^tagml")
    val tagmlUri: URI
        get() = URI.create(uriBase + ResourcePaths.DOCUMENTS_TAGML)

    @get:JsonProperty("^overview")
    val laTeX1: URI
        get() = URI.create(uriBase + ResourcePaths.DOCUMENTS_LATEX)

    @get:JsonProperty("^markupdepth")
    val markupDepthURI: URI
        get() = URI.create(uriBase + ResourcePaths.DOCUMENTS_MARKUPDEPTH)

    @get:JsonProperty("^matrix")
    val matrixURI: URI
        get() = URI.create(uriBase + ResourcePaths.DOCUMENTS_MATRIX)

    @get:JsonProperty("^kdtree")
    val kdTreeURI: URI
        get() = URI.create(uriBase + ResourcePaths.DOCUMENTS_KDTREE)

}
