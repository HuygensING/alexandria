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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ValidateCommand;
import org.junit.Test;

public class ValidateCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new ValidateCommand().getName();

  @Test
  public void testCommandWihValidInput() throws Exception {
    runInitCommand();
    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test [w>word<w]<l]<tagml]";
    String tagPath = createFile(tagFilename, tagml);
    // create schema sourcefile
    String schemaFilename = "schema.yaml";
    String yaml = "---\n" + "$:\n" + "  tagml:\n" + "    - l:\n" + "      - w\n";
    createFile(schemaFilename, yaml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final boolean success = cli.run(command, "-d", "transcription", "-s", schemaFilename);
    String expectedOutput =
        "Document transcription is \n"
            + "  valid\n"
            + "according to the schema defined in schema.yaml";
    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
    //    assertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandWithInvalidInput() throws Exception {
    runInitCommand();
    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[a>[aa>test [aaa>word<aaa]<aa]<a]";
    String tagPath = createFile(tagFilename, tagml);
    // create schema sourcefile
    String schemaFilename = "schema.yaml";
    String yaml = "---\n" + "$:\n" + "  a:\n" + "    - bb:\n" + "      - aaa\n";
    createFile(schemaFilename, yaml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final boolean success = cli.run(command, "-d", "transcription", "-s", schemaFilename);
    String expectedOutputError =
        "Document transcription is \n"
            + "  not valid:\n"
            + "  - Layer (default): expected [bb> as child markup of [a>, but found [aa>\n"
            + "according to the schema defined in schema.yaml";
    softlyAssertSucceedsWithExpectedStdout(success, expectedOutputError);
    //    assertSucceedsWithExpectedStdout(success, expectedOutputError);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       schema-validate -d DOCUMENT -s SCHEMA [-h]\n"
            + "\n"
            + "Validate a document against a TAG schema.\n"
            + "\n"
            + "named arguments:\n"
            + "  -d DOCUMENT, --document DOCUMENT\n"
            + "                         The name of the document to validate.\n"
            + "  -s SCHEMA, --schema SCHEMA\n"
            + "                         The TAG schema file.\n"
            + "  -h, --help             show this help message and exit");
  }
}
