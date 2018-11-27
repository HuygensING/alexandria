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

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class CommitCommandIntegrationTest extends CommandIntegrationTest {
  //  @Ignore
  @Test
  public void testCommitCommandWithFile() throws Exception {
    runInitCommand();

    String filename = "transcription1.tagml";
    createFile(filename, "[tagml>test<tagml]");
    runAddCommand(filename);

    Instant dateAfterAdd = readLastCommittedInstant(filename);
    assertThat(dateAfterAdd).isNotNull();

    LOG.info("{}", dateAfterAdd);

    final boolean success = cli.run("commit", filename);

    softlyAssertSucceedsWithExpectedStdout(success, "Parsing transcription1.tagml to document transcription1...\ndone!");
    Instant dateAfterCommit = readLastCommittedInstant(filename);
    assertThat(dateAfterCommit).isAfter(dateAfterAdd);

  }

  @Test
  public void testCommitCommandWithAllOption() throws Exception {
    runInitCommand();
    String filename = "transcription1.tagml";
    createFile(filename, "[tagml>test<tagml]");

    final boolean success = cli.run("commit", "-a");
    softlyAssertSucceedsWithExpectedStdout(success, "done!");
  }

  @Test
  public void testCommitCommandHelp() throws Exception {
    final boolean success = cli.run("commit", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       commit [-a] [-h] [FILE [FILE ...]]\n" +
        "\n" +
        "Record changes to the repository\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the changed file(s)\n" +
        "\n" +
        "named arguments:\n" +
        "  -a                     automatically  add  all  changed  files  (default:\n" +
        "                         false)\n" +
        "  -h, --help             show this help message and exit");
  }

}
