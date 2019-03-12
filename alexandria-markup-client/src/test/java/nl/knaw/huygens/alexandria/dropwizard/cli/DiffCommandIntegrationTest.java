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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.DiffCommand;
import org.junit.Test;

public class DiffCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new DiffCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test [w>word<w]<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    // overwrite sourcefile
    String tagml2 = "[tagml>[l>example [x>word<x]<l]<tagml]";
    modifyFile(tagFilename, tagml2);

    final boolean success = cli.run(command, tagFilename);
    String expectedOutput = "diff for tagml/transcription.tagml:\n" +
        " [tagml>[l>\n" +
        "-test [w>\n" +
        "+example [x>\n" +
        " word\n" +
        "-<w]\n" +
        "+<x]\n" +
        " <l]<tagml]\n" +
        "\n" +
        "markup diff:\n" +
        "[w](2-2) replaced by [x](2-2)";
//    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
    assertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandWithMachineReadableOutput() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test [w>word<w]<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    // overwrite sourcefile
    String tagml2 = "[tagml>[l>example [x>word<x]<l]<tagml]";
    modifyFile(tagFilename, tagml2);

    final boolean success = cli.run(command, "-m", tagFilename);
    String expectedOutput = "~[5,x]";
//    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
    assertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       diff [-m] [-h] file\n" +
        "\n" +
        "Show the changes made to the file.\n" +
        "\n" +
        "positional arguments:\n" +
        "  file                   The file containing the edited view\n" +
        "\n" +
        "named arguments:\n" +
        "  -m                     Output  the  diff  in  a  machine-readable  format\n" +
        "                         (default: false)\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "something");
  }

}
