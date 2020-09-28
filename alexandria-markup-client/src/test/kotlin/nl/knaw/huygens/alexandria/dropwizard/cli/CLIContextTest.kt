package nl.knaw.huygens.alexandria.dropwizard.cli

/*-
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

import com.fasterxml.jackson.databind.ObjectMapper
import nl.knaw.huygens.alexandria.view.TAGViewDefinition
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.IOException
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class CLIContextTest {

    @Test
    @Throws(IOException::class)
    fun testSerialization() {
        val watchedFiles: Set<String> = setOf("transcriptions/transcription-1.tagml", "views/view-1.json")
        val watchedFilesMap: Map<String, FileInfo> = watchedFiles.stream()
                .collect(Collectors.toMap({ f: String? -> f }, { f: String? -> FileInfo().setLastCommit(Instant.now()) }) { a: FileInfo, b: FileInfo -> b })
        val tagViewDefinitionMap: MutableMap<String, TAGViewDefinition> = HashMap()
        val excludeALayer = TAGViewDefinition().withExcludeLayers(HashSet(listOf("a")))
        tagViewDefinitionMap["exclude-a-layer"] = excludeALayer
        val cliContext = CLIContext()
                .setTagViewDefinitions(tagViewDefinitionMap)
                .setActiveView("view-1")
                .setWatchedFiles(watchedFilesMap)
        val json = mapper.writeValueAsString(cliContext)
        assertThat(json).isNotEmpty

        println(json)
        val cliContext1 = mapper.readValue(json, CLIContext::class.java)
        assertThat(cliContext1).isEqualToComparingFieldByFieldRecursively(cliContext)
        assertThat(cliContext1.tagViewDefinitions["exclude-a-layer"]!!.excludeLayers)
                .containsExactly("a")
    }

    companion object {
        var mapper = ObjectMapper()

        init {
            mapper.findAndRegisterModules()
        }
    }
}
