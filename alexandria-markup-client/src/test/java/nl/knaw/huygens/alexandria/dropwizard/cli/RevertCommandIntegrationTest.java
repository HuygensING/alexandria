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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.RevertCommand;
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.StatusCommand;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RevertCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new RevertCommand().getName();

  @Test
  public void testCommandInMainView() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test<l]<tagml]";
    String tagPath = createFile(tagFilename, tagml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    // overwrite sourcefile
    String tagml2 = "[x>And now for something completely different.<x]";
    modifyFile(tagFilename, tagml2);

    String fileContentsBeforeRevert = readFileContents(tagFilename);
    assertThat(fileContentsBeforeRevert).isEqualTo(tagml2);

    final boolean success = cli.run(command, tagPath);
    assertSucceedsWithExpectedStdout(success, "Reverting " + tagFilename + "...\ndone!");

    String fileContentsAfterRevert = readFileContents(tagFilename);
    assertThat(fileContentsAfterRevert).isEqualTo(tagml);

    Boolean statusSuccess = cli.run(new StatusCommand().getName());
    assertThat(statusSuccess).isTrue();
    String stdOut = normalize(this.stdOut.toString());
    assertThat(stdOut).doesNotContain("modified: tagml/transcription.tagml");
  }

  @Test
  public void testCommandInView() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription1");
    String tagml = "[tagml>[l>test<l]<tagml]";
    String tagPath = createFile(tagFilename, tagml);

    // create viewfile
    String viewName = "l";
    String viewFilename = createViewFileName(viewName);
    String viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    runAddCommand(tagPath, viewPath);
    runCommitAllCommand();
    runCheckoutCommand(viewName);

    String tagml_l = "[l>test<l]";

    // overwrite sourcefile
    String tagml2 = "[x>And now for something completely different.<x]";
    modifyFile(tagFilename, tagml2);

    String fileContentsBeforeRevert = readFileContents(tagFilename);
    assertThat(fileContentsBeforeRevert).isEqualTo(tagml2);

    final boolean success = cli.run(command, tagPath);
    assertSucceedsWithExpectedStdout(success, "Reverting " + tagFilename + "...\ndone!");

    String fileContentsAfterRevert = readFileContents(tagFilename);
    assertThat(fileContentsAfterRevert).isEqualTo(tagml_l);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       revert [-h] <file> [<file> ...]\n" +
        "\n" +
        "Restore the document file(s).\n" +
        "\n" +
        "positional arguments:\n" +
        "  <file>                 the file to be reverted\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "something");
  }

}
