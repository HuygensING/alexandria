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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AddCommand;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new AddCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();
    String filename1 = "transcription1.tagml";
    String filename2 = "transcription2.tagml";
    createFile(filename1, "");
    createFile(filename2, "");
    final boolean success = cli.run(command, filename1, filename2);
    softlyAssertSucceedsWithExpectedStdout(success, "");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getWatchedFiles().keySet()).containsExactlyInAnyOrder(filename1, filename2);
  }

  @Test
  public void testCommandWithNonExistingFilesFails() throws Exception {
    runInitCommand();
    final boolean success = cli.run(command, "transcription1.tagml", "transcription2.tagml");
    assertThat(success).isTrue();
    assertThat(getCliStdErrAsString()).contains("transcription1.tagml is not a file!")
        .contains("transcription2.tagml is not a file!");
  }

  @Test
  public void testCommandWithoutParametersFails() throws Exception {
    final boolean success = cli.run(command);
    assertThat(getCliStdErrAsString()).contains("too few arguments");
    assertFailsWithExpectedStderr(success, "too few arguments\n" +
        "usage: java -jar alexandria-app.jar\n" +
        "       add [-h] FILE [FILE ...]\n" +
        "\n" +
        "Add file context to the index\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the files to watch\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       add [-h] FILE [FILE ...]\n" +
        "\n" +
        "Add file context to the index\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the files to watch\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "something");
  }

}
