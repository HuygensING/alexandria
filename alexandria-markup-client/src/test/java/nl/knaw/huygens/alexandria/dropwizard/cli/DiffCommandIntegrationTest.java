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
import org.junit.Ignore;
import org.junit.Test;

public class DiffCommandIntegrationTest extends CommandIntegrationTest {
  @Ignore
  @Test
  public void testDiffCommand() throws Exception {
    runInitCommand();
    final boolean success = cli.run("diff");
    softlyAssertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testDiffCommandHelp() throws Exception {
    final boolean success = cli.run("diff", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       diff [-h] file\n" +
        "\n" +
        "Show the changes made to the view.\n" +
        "\n" +
        "positional arguments:\n" +
        "  file                   The file containing the edited view\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

}
