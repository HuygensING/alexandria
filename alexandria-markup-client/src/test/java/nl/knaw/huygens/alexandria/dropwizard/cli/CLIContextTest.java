package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2019 Huygens ING (KNAW)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import nl.knaw.huygens.alexandria.view.TAGViewDefinition;
import org.junit.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

public class CLIContextTest {
  static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.findAndRegisterModules();
  }

  @Test
  public void testSerialization() throws IOException {
    final Set<String> watchedFiles = new HashSet<>(asList(
        "transcriptions/transcription-1.tagml",
        "views/view-1.json"
    ));
    Map<String, FileInfo> watchedFilesMap = watchedFiles.stream()
        .collect(toMap(f -> f, f -> new FileInfo().setLastCommit(Instant.now()), (a, b) -> b));
    final Map<String, TAGViewDefinition> tagViewDefinitionMap = new HashMap<>();
    TAGViewDefinition excludeALayer = new TAGViewDefinition()
        .setExcludeLayers(new HashSet(singletonList("a")));
    tagViewDefinitionMap.put("exclude-a-layer", excludeALayer);
    CLIContext cliContext = new CLIContext()
        .setTagViewDefinitions(tagViewDefinitionMap)
        .setActiveView("view-1")
        .setWatchedFiles(watchedFilesMap);
    String json = mapper.writeValueAsString(cliContext);
    assertThat(json).isNotEmpty();
    System.out.println(json);
    CLIContext cliContext1 = mapper.readValue(json, CLIContext.class);
    assertThat(cliContext1).isEqualToComparingFieldByFieldRecursively(cliContext);
    assertThat(cliContext1.getTagViewDefinitions().get("exclude-a-layer").getExcludeLayers()).containsExactly("a");
  }

}
