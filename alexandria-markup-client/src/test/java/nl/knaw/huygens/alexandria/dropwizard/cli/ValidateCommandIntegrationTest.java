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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ValidateCommand;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Optional;

public class ValidateCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new ValidateCommand().getName();

  @Test
  public void testCommandWithMissingDocument() throws Exception {
    runInitCommand();

    final Optional<Throwable> success = cli.run(command, "transcription");
    final String expectedOutput = "";
    final String expectedError =
        "ERROR: No document 'transcription' was registered.\n" + "Registered documents:";
    //    assertFailsWithExpectedStdoutAndStderr(success, expectedOutput, expectedError);
    softlyAssertFailsWithExpectedStderr(success, expectedError);
  }

  @Test
  public void testCommandWithMissingSchemaLocation() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test [w>word<w]<l]<tagml]";
    String tagPath = createFile(tagFilename, tagml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final Optional<Throwable> success = cli.run(command, "transcription");
    String expectedError =
        "There was no schema location defined in transcription, please add\n"
            + "  [!schema <schemaLocationURL>]\n"
            + "to the tagml sourcefile.";
    softlyAssertFailsWithExpectedStderr(success, expectedError);
    //    assertFailsWithExpectedStderr(success, expectedError);
  }

  @Test
  public void testCommandWithValidInput() throws Exception {
    runInitCommand();

    // create schema sourcefile
    String schemaFilename = "schema.yaml";
    String yaml = "---\n$:\n  tagml:\n    - l:\n      - w\n";
    final String schemaFile = createFile(schemaFilename, yaml);

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String schemaLocationURL = schemaLocationURL(schemaFile);
    String tagml = schemaLocationElement(schemaFile) + "[tagml>[l>test [w>word<w]<l]<tagml]";
    String tagPath = createFile(tagFilename, tagml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final Optional<Throwable> success = cli.run(command, "transcription");
    String expectedOutput =
        "Parsing schema from "
            + schemaLocationURL
            + ":\n"
            + "  done\n\n"
            + "Document transcription is \n"
            + "  valid\n\n"
            + "according to the schema defined in "
            + schemaLocationURL;
    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
    //    assertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandWithInvalidTAGMLInput() throws Exception {
    runInitCommand();

    // create schema sourcefile
    String schemaFilename = "schema.yaml";
    String yaml = "---\n$:\n  a:\n    - bb:\n      - aaa\n";
    final String schemaFile = createFile(schemaFilename, yaml);

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String schemaLocationURL = schemaLocationURL(schemaFile);
    String tagml = schemaLocationElement(schemaFile) + "[a>[aa>test [aaa>word<aaa]<aa]<a]";
    String tagPath = createFile(tagFilename, tagml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final Optional<Throwable> success = cli.run(command, "transcription");
    String expectedOutputError =
        "Parsing schema from "
            + schemaLocationURL
            + ":\n"
            + "  done\n\n"
            + "Document transcription is \n"
            + "  not valid:\n"
            + "  - error: Layer $ (default): expected [bb> as child markup of [a>, but found [aa>\n\n"
            + "according to the schema defined in "
            + schemaLocationURL;
    //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutputError);
    assertSucceedsWithExpectedStdout(success, expectedOutputError);
  }

  @Test
  public void testCommandWithInvalidSchema() throws Exception {
    runInitCommand();

    // create schema sourcefile
    String schemaFilename = "schema.yaml";
    String yaml = "%!invalid YAML@:";
    final String schemaFile = createFile(schemaFilename, yaml);

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String schemaLocationURL = schemaLocationURL(schemaFile);
    String tagml = schemaLocationElement(schemaFile) + "[a>[aa>test [aaa>word<aaa]<aa]<a]";
    String tagPath = createFile(tagFilename, tagml);

    runAddCommand(tagPath);
    runCommitAllCommand();

    final Optional<Throwable> success = cli.run(command, "transcription");
    String expectedOutputError =
        "Parsing schema from "
            + schemaLocationURL
            + ":\n"
            + "  errors:\n"
            + "  - while scanning a directive\n"
            + " in 'reader', line 1, column 1:\n"
            + "    %!invalid YAML@:\n"
            + "    ^\n"
            + "expected alphabetic or numeric character, but found !(33)\n"
            + " in 'reader', line 1, column 2:\n"
            + "    %!invalid YAML@:\n"
            + "     ^\n"
            + "\n"
            + " at [Source: "
            + schemaLocationURL
            + "; line: 1, column: 1]\n"
            + "  - no layer definitions found";
    //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutputError);
    assertSucceedsWithExpectedStdout(success, expectedOutputError);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final Optional<Throwable> success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(
        success,
        "usage: java -jar alexandria-app.jar\n"
            + "       schema-validate [-h] <document>\n"
            + "\n"
            + "Validate a document against a TAG schema.\n"
            + "\n"
            + "positional arguments:\n"
            + "  <document>             The name of  the  document  to  validate.  It must\n"
            + "                         have a valid URL to a  valid schema file (in YAML)\n"
            + "                         defined using  '[!schema  <schemaLocationURL>]' in\n"
            + "                         the TAGML source file.\n"
            + "\n"
            + "named arguments:\n"
            + "  -h, --help             show this help message and exit");
  }

  @NotNull
  private String schemaLocationElement(final String file) {
    final String url = schemaLocationURL(file);
    return "[!schema " + url + "]";
  }

  @NotNull
  private String schemaLocationURL(final String file) {
    return "file:///" + file.replaceAll("\\\\", "/");
  }
}
