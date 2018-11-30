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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ExportRenderedDotCommand;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class ExportSvgCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new ExportRenderedDotCommand("svg").getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    String tagFilename = "transcriptions/transcription.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    boolean success = cli.run(command,  "transcription");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isTrue();
    String svgContent = stdOut.toString();
    softly.assertThat(svgContent).as("stdout").isNotEmpty();
    softly.assertThat(svgContent).as("stdoutIsSvg").contains("<svg ");
    softly.assertThat(stdErr.toString().trim()).as("stderr").isEmpty();
    softly.assertAll();
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       export-svg [-h] DOCUMENT\n" +
        "\n" +
        "Export the document as svg.\n" +
        "\n" +
        "positional arguments:\n" +
        "  DOCUMENT               The name of the document to export.\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "document");
  }
}
