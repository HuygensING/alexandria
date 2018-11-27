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

import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testInitCommand() throws Exception {
    final boolean success = cli.run("init");
    softlyAssertSucceedsWithExpectedStdout(success, "initializing...\n" +
        "done!");

    Path viewsDir = workFilePath("views");
    assertThat(viewsDir).isDirectory()
        .isWritable();

    Path transcriptionsDir = workFilePath("transcriptions");
    assertThat(transcriptionsDir).isDirectory()
        .isWritable();

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo("-");
  }

  @Test
  public void testInitCommandHelp() throws Exception {
    final boolean success = cli.run("init", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       init [-h]\n" +
        "\n" +
        "Initializes current directory as an alexandria workspace\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

}
