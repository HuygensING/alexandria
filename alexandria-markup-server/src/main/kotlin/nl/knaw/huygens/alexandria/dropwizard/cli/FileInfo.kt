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

import java.time.Instant

class FileInfo {
    var fileType: FileType? = null
        private set

    var objectName: String? = null
        private set

    var lastCommit: Instant? = null
        internal set

    fun withFileType(fileType: FileType): FileInfo {
        this.fileType = fileType
        return this
    }

    fun withObjectName(objectName: String): FileInfo {
        this.objectName = objectName
        return this
    }

    fun withLastCommit(lastCommit: Instant): FileInfo {
        this.lastCommit = lastCommit
        return this
    }
}
