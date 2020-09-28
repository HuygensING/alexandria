package nl.knaw.huygens.alexandria.dropwizard


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

import com.fasterxml.jackson.annotation.JsonProperty
import io.dropwizard.Configuration
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration
import nl.knaw.huygens.alexandria.storage.TAGStore
import org.hibernate.validator.constraints.NotEmpty

class ServerConfiguration : Configuration() {
    @NotEmpty
    var baseURI: String? = null
        private set

    var store: TAGStore? = null

    var dbDir: String? = null

    fun setBaseURI(baseURI: String) {
        this.baseURI = baseURI.replaceFirst("/$".toRegex(), "")
    }

    @JsonProperty("swagger")
    var swaggerBundleConfiguration: SwaggerBundleConfiguration? = null
}
