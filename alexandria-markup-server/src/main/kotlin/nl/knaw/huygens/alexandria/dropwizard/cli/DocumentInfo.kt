package nl.knaw.huygens.alexandria.dropwizard.cli

/*-
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
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

class DocumentInfo {
    var documentName: String? = null
        private set

    var sourceFile: String? = null
        private set

    var dbId: Long? = null
        private set

    fun withDocumentName(documentName: String): DocumentInfo {
        this.documentName = documentName
        return this
    }

    fun withSourceFile(sourceFile: String): DocumentInfo {
        this.sourceFile = sourceFile
        return this
    }

    fun withDbId(dbId: Long): DocumentInfo {
        this.dbId = dbId
        return this
    }
}
