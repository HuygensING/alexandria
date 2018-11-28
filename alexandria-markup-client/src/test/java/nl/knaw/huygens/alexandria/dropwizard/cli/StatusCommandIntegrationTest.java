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

public class StatusCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testStatusCommand() throws Exception {
    runInitCommand();
    final boolean success = cli.run("status");
    softlyAssertSucceedsWithExpectedStdout(success, "Alexandria version $version$\n" +
        "Build date: $buildDate$\n" +
        "\n" +
        "Active view: -\n" +
        "\n" +
        "no documents\n" +
        "no views");
  }

  @Test
  public void testStatusCommandHelp() throws Exception {
    final boolean success = cli.run("status", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       status [-h]\n" +
        "\n" +
        "Show info about the alexandria graph and the directory status.\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory("status");
  }
}
