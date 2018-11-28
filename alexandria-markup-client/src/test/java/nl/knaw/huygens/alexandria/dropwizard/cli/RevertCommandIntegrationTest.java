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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.RevertCommand;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RevertCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new RevertCommand().getName();

  @Test
  public void testCommandInMainView() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = "transcriptions/transcription.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    // overwrite sourcefile
    String tagml2 = "[x>And now for something completely different.<x]";
    modifyFile(tagFilename, tagml2);

    String fileContentsBeforeRevert = readFileContents(tagFilename);
    assertThat(fileContentsBeforeRevert).isEqualTo(tagml2);

    final boolean success = cli.run(command, tagFilename);
    assertSucceedsWithExpectedStdout(success, "Reverting " + tagFilename + "...\ndone!");

    String fileContentsAfterRevert = readFileContents(tagFilename);
    assertThat(fileContentsAfterRevert).isEqualTo(tagml);
  }

  @Test
  public void testCommandInView() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = "transcription1.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    // create viewfile
    String viewName = "l";
    String viewFilename = "views/" + viewName + ".json";
    createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    runAddCommand(tagFilename, viewFilename);
    runCommitAllCommand();
    runCheckoutCommand(viewName);

    String tagml_l = "[l>test<l]";

    // overwrite sourcefile
    String tagml2 = "[x>And now for something completely different.<x]";
    modifyFile(tagFilename, tagml2);

    String fileContentsBeforeRevert = readFileContents(tagFilename);
    assertThat(fileContentsBeforeRevert).isEqualTo(tagml2);

    final boolean success = cli.run(command, tagFilename);
    assertSucceedsWithExpectedStdout(success, "Reverting " + tagFilename + "...\ndone!");

    String fileContentsAfterRevert = readFileContents(tagFilename);
    assertThat(fileContentsAfterRevert).isEqualTo(tagml_l);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       revert [-h] FILE [FILE ...]\n" +
        "\n" +
        "Restore the document file(s).\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the file to be reverted\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "something");
  }

}
