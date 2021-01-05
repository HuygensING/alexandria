package nl.knaw.huygens.alexandria.dropwizard.cli


/*-
* #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2021 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.DiffCommand
import org.junit.Test

class DiffCommandIntegrationTest : CommandIntegrationTest() {
    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test [w>word<w]<l]<tagml]"
        val file = createFile(tagFilename, tagml)
        runAddCommand(file)
        runCommitAllCommand()

        // overwrite sourcefile
        val tagml2 = "[tagml>[l>example [x>word<x]<l]<tagml]"
        modifyFile(tagFilename, tagml2)
        val success = cli!!.run(command, tagFilename)
        val expectedOutput = """diff for tagml/transcription.tagml:
 [tagml>[l>
-test [w>
+example [x>
 word
-<w]
+<x]
 <l]<tagml]

markup diff:
[w](2-2) replaced by [x](2-2)"""
        //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
        assertSucceedsWithExpectedStdout(success, expectedOutput)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandWithMachineReadableOutput() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test [w>word<w]<l]<tagml]"
        val filePath = createFile(tagFilename, tagml)
        runAddCommand(filePath)
        runCommitAllCommand()

        // overwrite sourcefile
        val tagml2 = "[tagml>[l>example [x>word<x]<l]<tagml]"
        modifyFile(tagFilename, tagml2)
        val success = cli!!.run(command, "-m", tagFilename)
        val expectedOutput = "~[5,x]"
        //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
        assertSucceedsWithExpectedStdout(success, expectedOutput)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                success,
                """usage: java -jar alexandria-app.jar
       diff [-m] [-h] file

Show the changes made to the file.

positional arguments:
  file                   The file containing the edited view

named arguments:
  -m                     Output  the  diff  in  a  machine-readable  format
                         (default: false)
  -h, --help             show this help message and exit""")
    }

    @Test
    @Throws(Exception::class)
    fun testCommandShouldBeRunInAnInitializedDirectory() {
        assertCommandRunsInAnInitializedDirectory(command, "something")
    }

    companion object {
        private val command = DiffCommand().name
    }
}
