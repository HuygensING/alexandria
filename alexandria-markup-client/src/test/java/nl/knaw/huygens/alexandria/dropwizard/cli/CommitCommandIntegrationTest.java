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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CommitCommand;
import org.junit.Ignore;
import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CommitCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new CommitCommand().getName();

  @Test
  public void testCommandWithBadViewDefinitionThrowsError() throws Exception {
    runInitCommand();

    String viewFilename = createViewFileName("v1");
    String viewPath = createFile(viewFilename, "{\"idontknowwhatimdoing\":[\"huh?\"]}");
    runAddCommand(viewPath);

    final boolean success = cli.run(command, "-a");

    softlyAssertFailsWithExpectedStderr(
        success,
        "Commit aborted: Invalid view definition in views/v1.json: none of the allowed options includeLayers, excludeLayers, includeMarkup or excludeMarkup was found.");
  }

  @Test
  public void testCommandWithoutFileThrowsError() throws Exception {
    runInitCommand();

    final boolean success = cli.run(command);

    softlyAssertFailsWithExpectedStderr(
        success, "Commit aborted: no files specified. Use -a to commit all changed tracked files.");
  }

  @Test
  public void testCommandWithFile() throws Exception {
    runInitCommand();

    String filename = "transcription1.tagml";
    String absolutePath = createFile(filename, "[tagml>test<tagml]");
    runAddCommand(absolutePath);

    Instant dateAfterAdd = readLastCommittedInstant(filename);
    assertThat(dateAfterAdd).isNotNull();

    LOG.info("{}", dateAfterAdd);

    final boolean success = cli.run(command, absolutePath);

    softlyAssertSucceedsWithExpectedStdout(
        success, "Parsing transcription1.tagml to document transcription1...\ndone!");
    Instant dateAfterCommit = readLastCommittedInstant(filename);
    assertThat(dateAfterCommit).isAfter(dateAfterAdd);
  }

  @Test
  public void testCommandWithAllOption() throws Exception {
    runInitCommand();
    String tagFilename = createTagmlFileName("transcription1");
    String tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]");
    String viewFilename = createViewFileName("v1");
    String viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");

    runAddCommand(tagPath, viewPath);

    Instant tagDateAfterAdd = readLastCommittedInstant(tagFilename);
    assertThat(tagDateAfterAdd).isNotNull();
    Instant viewDateAfterAdd = readLastCommittedInstant(viewFilename);
    assertThat(viewDateAfterAdd).isNotNull();

    final boolean success = cli.run(command, "-a");
    softlyAssertSucceedsWithExpectedStdout(
        success,
        "Parsing tagml/transcription1.tagml to document transcription1...\n"
            + "Parsing views/v1.json to view v1...\n"
            + "done!");

    Instant tagDateAfterCommit = readLastCommittedInstant(tagFilename);
    assertThat(tagDateAfterCommit).isAfter(tagDateAfterAdd);
    Instant viewDateAfterCommit = readLastCommittedInstant(viewFilename);
    assertThat(viewDateAfterCommit).isAfter(viewDateAfterAdd);
  }

  @Ignore("fails on jenkins")
  @Test
  public void testCommitWithActiveView() throws Exception {
    runInitCommand();
    String tagFilename = createTagmlFileName("transcription1");
    String tagPath = createFile(tagFilename, "[tagml>[l>test<l]<tagml]");
    String viewFilename = createViewFileName("v1");
    String viewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");

    runAddCommand(tagPath, viewPath);

    Instant tagDateAfterAdd = readLastCommittedInstant(tagFilename);
    assertThat(tagDateAfterAdd).isNotNull();
    Instant viewDateAfterAdd = readLastCommittedInstant(viewFilename);
    assertThat(viewDateAfterAdd).isNotNull();

    final boolean success = cli.run(command, "-a");
    softlyAssertSucceedsWithExpectedStdout(
        success,
        "Parsing tagml/transcription1.tagml to document transcription1...\n"
            + "Parsing views/v1.json to view v1...\n"
            + "done!");

    Instant tagDateAfterCommit = readLastCommittedInstant(tagFilename);
    assertThat(tagDateAfterCommit).isAfter(tagDateAfterAdd);
    Instant viewDateAfterCommit = readLastCommittedInstant(viewFilename);
    assertThat(viewDateAfterCommit).isAfter(viewDateAfterAdd);

    // alexandria checkout v1
    runCheckoutCommand("v1");

    // create new transcription & view
    String tagFilename2 = createTagmlFileName("transcription2");
    String tagPath2 = createFile(tagFilename2, "[tagml>[l>Hello World<l]<tagml]");
    String viewFilename2 = createViewFileName("v2");
    String viewPath2 = createFile(viewFilename2, "{\"includeMarkup\":[\"m\"]}");
    runAddCommand(tagPath2, viewPath2);

    modifyFile(tagFilename, "[tagml>[p>Hello world!<p]<tagml]");
    //    modifyFile(viewFilename, "{\"includeMarkup\":[\"p\"]}");

    //    final boolean success2 = cli.run("status");
    //    assertSucceedsWithExpectedStdout(success2, "");

    final boolean success3 = cli.run(command, "-a");
    assertFailsWithExpectedStdoutAndStderr(
        success3,
        "Parsing tagml/transcription2.tagml to document transcription2...\n"
            + "Parsing views/v2.json to view v2...",
        "unable to commit tagml/transcription1.tagml\n"
            + "View v1 is active. Currently, committing changes to existing documents is only allowed in the main view. Use:\n"
            + "  alexandria revert tagml/transcription1.tagml\n"
            + "  alexandria checkout -\n"
            + "to undo those changes and return to the main view.\n"
            + "some commits failed");
  }

  @Test
  public void testCommittingANewViewDefinitionInAnOpenViewShouldWork() throws Exception {
    runInitCommand();

    // setup: add a file and viewdefinition v1
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test<l]<tagml]";
    String absoluteTagmlPath = createFile(tagFilename, tagml);

    String viewName = "v1";
    String viewFilename = createViewFileName(viewName);
    String absoluteViewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");

    runAddCommand(absoluteTagmlPath, absoluteViewPath);
    runCommitAllCommand();

    // checkout view v1
    runCheckoutCommand("v1");

    String viewName2 = "v2";
    String viewFilename2 = createViewFileName(viewName2);
    String absoluteViewPath2 = createFile(viewFilename2, "{\"excludeMarkup\":[\"l\"]}");

    runAddCommand(absoluteViewPath2);

    Instant dateAfterAdd = readLastCommittedInstant(viewFilename2);
    assertThat(dateAfterAdd).isNotNull();

    LOG.info("{}", dateAfterAdd);

    final boolean success = cli.run(command, absoluteViewPath2);

    softlyAssertSucceedsWithExpectedStdout(success, "Parsing views/v2.json to view v2...\ndone!");
    Instant dateAfterCommit = readLastCommittedInstant(viewFilename2);
    assertThat(dateAfterCommit).isAfter(dateAfterAdd);
    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getTagViewDefinitions().keySet()).containsOnly(viewName, viewName2);
  }

  @Test
  public void testCommittingAModifiedViewDefinitionForTheCurrentlyOpenedViewShouldGiveAnError()
      throws Exception {
    runInitCommand();

    // setup: add a file and viewdefinition v1
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test<l]<tagml]";
    String absoluteTagmlPath = createFile(tagFilename, tagml);

    String viewName = "v1";
    String viewFilename = createViewFileName(viewName);
    String absoluteViewPath = createFile(viewFilename, "{\"includeMarkup\":[\"l\"]}");

    runAddCommand(absoluteTagmlPath, absoluteViewPath);
    runCommitAllCommand();

    // checkout view v1
    runCheckoutCommand("v1");

    modifyFile(viewFilename, "{\"excludeMarkup\":[\"l\"]}");

    runAddCommand(absoluteViewPath);

    final boolean success = cli.run(command, absoluteViewPath);

    assertFailsWithExpectedStderr(
        success,
        "You are trying to modify the definition file "
            + viewFilename
            + " of the active view "
            + viewName
            + ". This is not allowed.\n\n"
            + "Use:\n"
            + "  alexandria revert views/v1.json\n"
            + "  alexandria checkout -\n"
            + "to undo those changes and return to the main view.\n"
            + "some commits failed");
    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getTagViewDefinitions().keySet()).containsOnly(viewName);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       commit [-a] [-h] [<file> [<file> ...]]\n"
            + "\n"
            + "Record changes to the repository.\n"
            + "\n"
            + "positional arguments:\n"
            + "  <file>                 the changed file(s)\n"
            + "\n"
            + "named arguments:\n"
            + "  -a                     automatically  add  all  changed  files  (default:\n"
            + "                         false)\n"
            + "  -h, --help             show this help message and exit\n"
            + "\n"
            + "Warning: currently, committing tagml changes  is  only possible in the main\n"
            + "view!");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "-a");
  }
}
