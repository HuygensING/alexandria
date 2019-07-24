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

import org.junit.Test;

import java.nio.file.Path;

import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand.SOURCE_DIR;
import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand.VIEWS_DIR;
import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = "init";

  @Test
  public void testCommand() throws Exception {
    final boolean success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "initializing...\n" +
        "done!");

    Path viewsDir = workFilePath(VIEWS_DIR);
    assertThat(viewsDir).isDirectory()
        .isWritable();

    Path transcriptionsDir = workFilePath(SOURCE_DIR);
    assertThat(transcriptionsDir).isDirectory()
        .isWritable();

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo("-");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       init [-h]\n" +
        "\n" +
        "Initializes current directory as an alexandria workspace.\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

}
