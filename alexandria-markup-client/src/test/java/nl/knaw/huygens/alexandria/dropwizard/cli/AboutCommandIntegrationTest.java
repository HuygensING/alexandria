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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AboutCommand;
import org.junit.Test;

public class AboutCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new AboutCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();
    final boolean success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Alexandria version $version$\n" + "Build date: $buildDate$");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       about [-h]\n"
            + "\n"
            + "Show version number and build date.\n"
            + "\n"
            + "named arguments:\n"
            + "  -h, --help             show this help message and exit");
  }
}
