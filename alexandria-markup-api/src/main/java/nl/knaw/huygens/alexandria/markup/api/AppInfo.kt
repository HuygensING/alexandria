package nl.knaw.huygens.alexandria.markup.api

/*
 * #%L
 * alexandria-markup-api
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

class AppInfo {
    var appName: String? = null
        private set
    var startedAt: String? = null
        private set
    var version: String? = null
        private set
    var buildDate: String? = null
        private set
    var commitId: String? = null
        private set
    var scmBranch: String? = null
        private set

    fun setAppName(appName: String): AppInfo {
        this.appName = appName
        return this
    }

    fun setStartedAt(startedAt: String): AppInfo {
        this.startedAt = startedAt
        return this
    }

    fun setVersion(version: String): AppInfo {
        this.version = version
        return this
    }

    fun setBuildDate(buildDate: String): AppInfo {
        this.buildDate = buildDate
        return this
    }

    fun setCommitId(commitId: String): AppInfo {
        this.commitId = commitId
        return this
    }

    fun setScmBranch(scmBranch: String): AppInfo {
        this.scmBranch = scmBranch
        return this
    }
}
