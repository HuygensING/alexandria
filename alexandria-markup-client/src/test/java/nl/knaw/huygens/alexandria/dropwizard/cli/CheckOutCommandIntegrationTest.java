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

import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.CheckOutCommand.MAIN_VIEW;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckOutCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testCheckOutCommand() throws Exception {
    runInitCommand();

    String tagFilename = "transcription1.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);
    String viewName = "v1";
    String viewFilename = "views/" + viewName + ".json";
    createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    runAddCommand(tagFilename, viewFilename);
    runCommitAllCommand();

    final boolean success = cli.run("checkout", viewName);
    softlyAssertSucceedsWithExpectedStdout(success, "Checking out view v1...\n" +
        "  updating transcription1.tagml...\n" +
        "done!");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo(viewName);

    String newContent = readFileContents(tagFilename);
    assertThat(newContent).isEqualTo("[l>test<l]");

    final boolean success2 = cli.run("checkout", MAIN_VIEW);
    softlyAssertSucceedsWithExpectedStdout(success2, "Checking out main view...\n" +
        "  updating transcription1.tagml...\n" +
        "done!");

    CLIContext cliContext2 = readCLIContext();
    assertThat(cliContext2.getActiveView()).isEqualTo(MAIN_VIEW);

    String newContent2 = readFileContents(tagFilename);
    assertThat(newContent2).isEqualTo(tagml);
  }

  // On checkout, the lastcommitted dates should be adjusted.

  @Test
  public void testCheckOutCommandHelp() throws Exception {
    final boolean success = cli.run("checkout", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       checkout [-h] VIEW\n" +
        "\n" +
        "Activate or deactivate a view in this directory\n" +
        "\n" +
        "positional arguments:\n" +
        "  VIEW                   The name of the view to use\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory("checkout", "-");
  }

}
