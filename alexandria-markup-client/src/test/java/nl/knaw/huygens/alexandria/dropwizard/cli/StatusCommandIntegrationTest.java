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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.StatusCommand;
import org.junit.Test;

import java.nio.file.Files;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new StatusCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    boolean success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n");

    // add a sourcefile and a view definition
    String tagFilename = "transcriptions/transcription.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);
    String viewName = "l";
    String viewFilename = "views/" + viewName + ".json";
    createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n" +
        "\n" +
        "Untracked files:\n" +
        "  (use \"alexandria add <file>...\" to start tracking this file.)\n" +
        "\n" +
        "        transcriptions/transcription.tagml\n" +
        "        views/l.json");

    // add files
    runAddCommand(tagFilename, viewFilename);
    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n" +
        "\n" +
        "Uncommitted changes:\n" +
        "  (use \"alexandria commit <file>...\" to commit the selected changes)\n" +
        "  (use \"alexandria commit -a\" to commit all changes)\n" +
        "  (use \"alexandria revert <file>...\" to discard changes)\n" +
        "\n" +
        "        modified: transcriptions/transcription.tagml\n" +
        "        modified: views/l.json");

    // commit files
    runCommitAllCommand();
    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n");

    // checkout view
    runCheckoutCommand(viewName);
    success = cli.run(command);

    CLIContext cliContext2 = readCLIContext();
    Instant lastCommit = cliContext2.getWatchedFiles().get(tagFilename).getLastCommit();
    Instant lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant();
    assertThat(lastCommit.isAfter(lastModified));

    softlyAssertSucceedsWithExpectedStdout(success, "Active view: l\n");

    // checkout main view and change a file
    runCheckoutCommand("-");
    String newTagml = "[tagml>something else<tagml]";
    modifyFile(tagFilename, newTagml);
    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n" +
        "\n" +
        "Uncommitted changes:\n" +
        "  (use \"alexandria commit <file>...\" to commit the selected changes)\n" +
        "  (use \"alexandria commit -a\" to commit all changes)\n" +
        "  (use \"alexandria revert <file>...\" to discard changes)\n" +
        "\n" +
        "        modified: transcriptions/transcription.tagml");

    // delete file
    deleteFile(tagFilename);
    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "Active view: -\n" +
        "\n" +
        "Uncommitted changes:\n" +
        "  (use \"alexandria commit <file>...\" to commit the selected changes)\n" +
        "  (use \"alexandria commit -a\" to commit all changes)\n" +
        "  (use \"alexandria revert <file>...\" to discard changes)\n" +
        "\n" +
        "        deleted:  transcriptions/transcription.tagml");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       status [-h]\n" +
        "\n" +
        "Show the directory status (active view, modified files, etc.).\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command);
  }
}
