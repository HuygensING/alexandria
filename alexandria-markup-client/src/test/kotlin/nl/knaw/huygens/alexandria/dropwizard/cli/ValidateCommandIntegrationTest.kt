package nl.knaw.huygens.alexandria.dropwizard.cli

/*-
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ValidateCommand
import org.junit.Ignore
import org.junit.Test

@Ignore()
class ValidateCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommandWithMissingDocument() {
        runInitCommand()
        val success = cli!!.run(command, "transcription")
        val expectedOutput = ""
        val expectedError = """
            ERROR: No document 'transcription' was registered.
            Registered documents:
            """.trimIndent()
        //    assertFailsWithExpectedStdoutAndStderr(success, expectedOutput, expectedError);
        softlyAssertFailsWithExpectedStderr(success, expectedError)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithMissingSchemaLocation() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test [w>word<w]<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        val expectedError = """There was no schema location defined in transcription, please add
  [!schema <schemaLocationURL>]
to the tagml sourcefile."""
        softlyAssertFailsWithExpectedStderr(success, expectedError)
        //    assertFailsWithExpectedStderr(success, expectedError);
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithValidInput() {
        runInitCommand()

        // create schema sourcefile
        val schemaFilename = "schema.yaml"
        val yaml = """
            |---
            |$:
            |  tagml:
            |    - l:
            |      - w
            |""".trimMargin()
        val schemaFile = createFile(schemaFilename, yaml)

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val schemaLocationURL = schemaLocationURL(schemaFile)
        val tagml = schemaLocationElement(schemaFile) + "[tagml>[l>test [w>word<w]<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        val expectedOutput = """Parsing schema from $schemaLocationURL:
  done

Document transcription is 
  valid

according to the schema defined in $schemaLocationURL"""
        softlyAssertSucceedsWithExpectedStdout(success, expectedOutput)
        //    assertSucceedsWithExpectedStdout(success, expectedOutput);
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithInvalidTAGMLInput() {
        runInitCommand()

        // create schema sourcefile
        val schemaFilename = "schema.yaml"
        val yaml = "---\n$:\n  a:\n    - bb:\n      - aaa\n"
        val schemaFile = createFile(schemaFilename, yaml)

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val schemaLocationURL = schemaLocationURL(schemaFile)
        val tagml = schemaLocationElement(schemaFile) + "[a>[aa>test [aaa>word<aaa]<aa]<a]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        val expectedOutputError = """Parsing schema from $schemaLocationURL:
  done

Document transcription is 
  not valid:
  - error: Layer $ (default): expected [bb> as child markup of [a>, but found [aa>

according to the schema defined in $schemaLocationURL"""
        //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutputError);
        assertSucceedsWithExpectedStdout(success, expectedOutputError)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithInvalidSchema() {
        runInitCommand()

        // create schema sourcefile
        val schemaFilename = "schema.yaml"
        val yaml = "%!invalid YAML@:"
        val schemaFile = createFile(schemaFilename, yaml)

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val schemaLocationURL = schemaLocationURL(schemaFile)
        val tagml = schemaLocationElement(schemaFile) + "[a>[aa>test [aaa>word<aaa]<aa]<a]"
        val tagPath = createFile(tagFilename, tagml)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription")
        val expectedOutputError = """Parsing schema from $schemaLocationURL:
  errors:
  - while scanning a directive
 in 'reader', line 1, column 1:
    %!invalid YAML@:
    ^
expected alphabetic or numeric character, but found !(33)
 in 'reader', line 1, column 2:
    %!invalid YAML@:
     ^

 at [Source: $schemaLocationURL; line: 1, column: 1]
  - no layer definitions found"""
        //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutputError);
        assertSucceedsWithExpectedStdout(success, expectedOutputError)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
            success,
            """usage: java -jar alexandria-app.jar
       schema-validate [-h] <document>

Validate a document against a TAG schema.

positional arguments:
  <document>             The name of  the  document  to  validate.  It must
                         have a valid URL to a  valid schema file (in YAML)
                         defined using  '[!schema  <schemaLocationURL>]' in
                         the TAGML source file.

named arguments:
  -h, --help             show this help message and exit"""
        )
    }

    companion object {
        private val command = ValidateCommand().name

        private fun schemaLocationURL(file: String): String = "file:///${file.replace("\\\\".toRegex(), "/")}"

        private fun schemaLocationElement(file: String): String {
            val url = schemaLocationURL(file)
            return "[!schema $url]"
        }
    }
}
