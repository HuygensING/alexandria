package nl.knaw.huygens.alexandria.dropwizard.cli

/*-
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

import nl.knaw.huygens.alexandria.view.TAGViewDefinition
import java.util.*

class CLIContext {
    var activeView = "-"
        internal set

    var watchedDirectories: MutableSet<String> = HashSet()

    var watchedFiles: MutableMap<String, FileInfo> = HashMap()
        private set

    var tagViewDefinitions: Map<String, TAGViewDefinition> = HashMap()

    var documentInfo: MutableMap<String, DocumentInfo> = HashMap()
        private set

    fun withActiveView(activeView: String): CLIContext {
        this.activeView = activeView
        return this
    }

    fun withWatchedFiles(watchedFiles: MutableMap<String, FileInfo>): CLIContext {
        this.watchedFiles = watchedFiles
        return this
    }

    fun withTagViewDefinitions(tagViewDefinitions: Map<String, TAGViewDefinition>): CLIContext {
        this.tagViewDefinitions = tagViewDefinitions
        return this
    }

    fun withDocumentInfo(documentInfo: MutableMap<String, DocumentInfo>): CLIContext {
        this.documentInfo = documentInfo
        return this
    }

    fun getDocumentName(fileName: String): Optional<String> =
            documentInfo.values.stream()
                    .filter { di: DocumentInfo -> di.sourceFile == fileName }
                    .findFirst()
                    .map { obj: DocumentInfo -> obj.documentName }
}
