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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CheckOutCommand;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.time.Instant;

import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.CheckOutCommand.MAIN_VIEW;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckOutCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new CheckOutCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    String viewName = "v1";
    String viewFilename = createViewFileName(viewName);
    createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");

    runAddCommand(tagFilename, viewFilename);
    runCommitAllCommand();

    final boolean success = cli.run(command, viewName);
    softlyAssertSucceedsWithExpectedStdout(success, "Checking out view v1...\n" +
        "  updating tagml/transcription.tagml...\n" +
        "done!");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo(viewName);

    String newContent = readFileContents(tagFilename);
    assertThat(newContent).isEqualTo("[l>test<l]");

    final boolean success2 = cli.run(command, MAIN_VIEW);
    softlyAssertSucceedsWithExpectedStdout(success2, "Checking out main view...\n" +
        "  updating tagml/transcription.tagml...\n" +
        "done!");

    CLIContext cliContext2 = readCLIContext();
    assertThat(cliContext2.getActiveView()).isEqualTo(MAIN_VIEW);
    Instant lastCommit = cliContext2.getWatchedFiles().get(tagFilename).getLastCommit();
    Instant lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant();
    assertThat(lastCommit.isAfter(lastModified));

    String newContent2 = readFileContents(tagFilename);
    assertThat(newContent2).isEqualTo(tagml);
  }

  @Ignore("race condition? fails on Jenkins")
  @Test
  public void testCheckoutNotPossibleWithUncommittedFilesPresent() throws Exception {
    runInitCommand();

    String tagFilename = createTagmlFileName("transcription1");
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);
    String viewName = "v1";
    String viewFilename = createViewFileName(viewName);
    createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    runAddCommand(tagFilename, viewFilename);
    runCommitAllCommand();

    final boolean success = cli.run(command, viewName);
    softlyAssertSucceedsWithExpectedStdout(success, "Checking out view v1...\n" +
        "  updating tagml/transcription1.tagml...\n" +
        "done!");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo(viewName);

    String newContent = readFileContents(tagFilename);
    assertThat(newContent).isEqualTo("[l>test<l]");

    // now, change the file contents
    modifyFile(tagFilename, "[l>foo bar<l]");

    final boolean success2 = cli.run(command, MAIN_VIEW);
    String stdOut = normalize(getCliStdOutAsString());
    assertThat(stdOut).isEqualTo("Uncommitted changes:\n" +
        "  (use \"alexandria commit <file>...\" to commit the selected changes)\n" +
        "  (use \"alexandria commit -a\" to commit all changes)\n" +
        "  (use \"alexandria revert <file>...\" to discard changes)\n" +
        "\n" +
        "        modified: tagml/transcription1.tagml");
    softlyAssertFailsWithExpectedStderr(success2, "Uncommitted changes found, cannot checkout another view.");
  }

  // On checkout, the lastcommitted dates should be adjusted.

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       checkout [-h] <view>\n" +
        "\n" +
        "Activate or deactivate a view in this directory.\n" +
        "\n" +
        "positional arguments:\n" +
        "  <view>                 The name of the view to use\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "-");
  }

}
