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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.DiffCommand;
import org.junit.Test;

public class DiffCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new DiffCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = "transcriptions/transcription.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    // overwrite sourcefile
    String tagml2 = "[tagml>[l>example<l]<tagml]";
    modifyFile(tagFilename, tagml2);

    final boolean success = cli.run(command, tagFilename);
    String expectedOutput = "diff for transcriptions/transcription.tagml:\n" +
        " [tagml>[l>\n" +
        "-test\n" +
        "+example\n" +
        " <l]<tagml]";
    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
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

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "something");
  }

}
