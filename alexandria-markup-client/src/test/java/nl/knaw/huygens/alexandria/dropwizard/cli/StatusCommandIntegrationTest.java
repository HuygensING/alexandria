package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
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
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new StatusCommand().getName();

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    Path currentPath = Paths.get("").toAbsolutePath();

    // add a sourcefile and a view definition
    String tagFilename = createTagmlFileName("transcription");
    String tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]");
    Path tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath));

    String viewName = "l";
    String viewFilename = createViewFileName(viewName);
    String viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");
    Path viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath));

    success = cli.run(command);
    assertSucceedsWithExpectedStdout(
        success,
        "Active view: -\n"
            + "\n"
            + "no documents\n"
            + "no views\n"
            + "\n"
            + "Uncommitted changes:\n"
            + "  (use \"alexandria commit <file>...\" to commit the selected changes)\n"
            + "  (use \"alexandria commit -a\" to commit all changes)\n"
            + "  (use \"alexandria revert <file>...\" to discard changes)\n"
            + "\n"
            + "        modified: "
            + tagPathRelativeToCurrentDir
            + "\n"
            + "        modified: "
            + viewPathRelativeToCurrentDir);

    // commit files
    runCommitAllCommand();
    success = cli.run(command);
    softlyAssertSucceedsWithStdoutContaining(success, "Active view: -\n");

    // checkout view
    runCheckoutCommand(viewName);
    success = cli.run(command);

    CLIContext cliContext2 = readCLIContext();
    Instant lastCommit = cliContext2.getWatchedFiles().get(tagFilename).getLastCommit();
    Instant lastModified = Files.getLastModifiedTime(workFilePath(tagFilename)).toInstant();
    assertThat(lastCommit.isAfter(lastModified));

    softlyAssertSucceedsWithStdoutContaining(success, "Active view: l\n");

    // checkout main view and change a file
    runCheckoutCommand("-");
    String newTagml = "[tagml>something else<tagml]";
    modifyFile(tagFilename, newTagml);
    success = cli.run(command);
    softlyAssertSucceedsWithStdoutContaining(
        success,
        "Active view: -\n"
    );

    // delete file
    deleteFile(tagFilename);
    success = cli.run(command);
    softlyAssertSucceedsWithStdoutContaining(
        success,
        "Active view: -\n"
    );
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testTagmlFileInRootShownAsUntrackedFile() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    Path currentPath = Paths.get("").toAbsolutePath();

    String tagFilename = "transcription.tag";
    String tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]");
    Path tagPathRelativeToCurrentDir = currentPath.relativize(Paths.get(tagPath));

    success = cli.run(command);
    softlyAssertSucceedsWithStdoutContaining(
        success,
        "Untracked files:\n"
            + "  (use \"alexandria add <file>...\" to start tracking this file.)\n"
            + "\n"
            + "        "
            + tagPathRelativeToCurrentDir);
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testJsonFileInRootShownAsUntrackedFile() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    Path currentPath = Paths.get("").toAbsolutePath();

    String viewFilename = "viewdef.json";
    String viewPath = createFile(viewFilename, "{}");
    Path viewPathRelativeToCurrentDir = currentPath.relativize(Paths.get(viewPath));

    success = cli.run(command);
    softlyAssertSucceedsWithStdoutContaining(
        success,
        "Untracked files:\n"
            + "  (use \"alexandria add <file>...\" to start tracking this file.)\n"
            + "\n"
            + "        "
            + viewPathRelativeToCurrentDir);
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testOtherFileInRootNotShownAsUntrackedFile() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    String otherFilename = "other.md";
    createFile(otherFilename, "bla bla bla");

    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views");
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testDirectoryWithTagmlFileShownAsUntrackedFile() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    Path currentPath = Paths.get("").toAbsolutePath();

    String directoryName = "subdir";
    String directoryPath = createDirectory(directoryName);
    Path directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath));

    String tagFilename = "subdir/transcription.tag";
    createFile(tagFilename, "[tagml>[l>test<l]<tagml]");

    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success,
        "Active view: -\n"
            + "\n"
            + "no documents\n"
            + "no views\n"
            + "\n"
            + "\n"
            + "Untracked files:\n"
            + "  (use \"alexandria add <file>...\" to start tracking this file.)\n"
            + "\n"
            + "        "
            + directoryPathRelativeToCurrentDir);
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testDirectoryStartingWithPointWithTagmlFileNotShownAsUntrackedFile()
      throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    Path currentPath = Paths.get("").toAbsolutePath();

    String directoryName = ".subdir";
    String directoryPath = createDirectory(directoryName);
    Path directoryPathRelativeToCurrentDir = currentPath.relativize(Paths.get(directoryPath));

    String tagFilename = ".subdir/transcription.tag";
    createFile(tagFilename, "[tagml>[l>test<l]<tagml]");

    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views");
  }

  @Ignore("Works fine in isolation, but otherwise has a problem with empty stdOut")
  @Test
  public void testDirectoryWithoutTagmlOrJsonFileNotShownAsUntrackedFile() throws Exception {
    runInitCommand();

    // in an empty, initialized directory
    Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views\n");

    String directoryName = "subdir";
    createDirectory(directoryName);

    String otherFilename = "subdir/other.txt";
    createFile(otherFilename, "Don't feed them after dark.");

    success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success, "Active view: -\n" + "\n" + "no documents\n" + "no views");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final Boolean  success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       status [-h]\n"
            + "\n"
            + "Show the directory status (active view, modified files, etc.).\n"
            + "\n"
            + "named arguments:\n"
            + "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command);
  }
}
