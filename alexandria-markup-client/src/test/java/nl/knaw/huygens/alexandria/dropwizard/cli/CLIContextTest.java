package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
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
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class CLIContextTest {

  @Test
  public void testSerialization() throws IOException {
    final Set<String> watchedFiles = new HashSet<>(asList(
        "transcriptions/transcription-1.tagml",
        "views/view-1.json"
    ));
    CLIContext cliContext = new CLIContext()
        .setViewName("filename", "viewname")
        .setDocumentName("filename", "docName")
        .setActiveView("view-1")
        .setWatchedFiles(watchedFiles);
    String json = new ObjectMapper().writeValueAsString(cliContext);
    assertThat(json).isNotEmpty();
    System.out.println(json);
    CLIContext cliContext1 = new ObjectMapper().readValue(json, CLIContext.class);
    assertThat(cliContext1).isEqualToComparingFieldByFieldRecursively(cliContext);
  }
}
