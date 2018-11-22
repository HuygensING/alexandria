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

public class CommitCommandIntegrationTest extends CommandIntegrationTest {
  @Ignore
  @Test
  public void testCommitCommand() throws Exception {
    runInitCommand();
    final boolean success = cli.run("commit");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testCommitCommandHelp() throws Exception {
    final boolean success = cli.run("commit", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       commit [-a A] [-h] FILE [FILE ...]\n" +
        "\n" +
        "Record changes to the repository\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the changed file(s)\n" +
        "\n" +
        "named arguments:\n" +
        "  -a A                   automatically add all changed files\n" +
        "  -h, --help             show this help message and exit");
  }

}
