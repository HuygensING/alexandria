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

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand.*;
import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = "init";

  @Test
  public void testCommandFailsWhenCurrentDirIsNotWritable() throws Exception {
    File workDirectoryFile = workDirectory.toFile();
    boolean asFile = workDirectory.resolve(".alexandria").toFile().createNewFile();
    assertThat(asFile).isTrue();
    final Boolean  success = cli.run(command);
    assertFailsWithExpectedStderr(
        success, "init failed: could not create directory " + workDirectory.resolve(".alexandria"));
  }

  @Test
  public void testCommand() throws Exception {
    final Boolean  success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(
        success,
        "initializing...\n"
            + "  mkdir "
            + workDirectory.resolve(".alexandria")
            + "\n"
            + "  mkdir "
            + workDirectory.resolve("tagml")
            + "\n"
            + "  mkdir "
            + workDirectory.resolve("views")
            + "\n"
            + "  mkdir "
            + workDirectory.resolve("sparql")
            + "\n"
            + "done!");

    Path viewsDir = workFilePath(VIEWS_DIR);
    assertThat(viewsDir).isDirectory().isWritable();

    Path transcriptionsDir = workFilePath(SOURCE_DIR);
    assertThat(transcriptionsDir).isDirectory().isWritable();

    Path sparqlDir = workFilePath(SPARQL_DIR);
    assertThat(sparqlDir).isDirectory().isWritable();

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo("-");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final Boolean  success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       init [-h]\n"
            + "\n"
            + "Initializes current directory as an alexandria workspace.\n"
            + "\n"
            + "named arguments:\n"
            + "  -h, --help             show this help message and exit");
  }
}
