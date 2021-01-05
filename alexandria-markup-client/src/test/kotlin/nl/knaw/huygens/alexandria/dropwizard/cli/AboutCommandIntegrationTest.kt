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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AboutCommand
import org.junit.Test

class AboutCommandIntegrationTest : CommandIntegrationTest() {

    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()
        val throwable = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                throwable,
                """
                Alexandria version ${"$"}version$
                Build date: ${"$"}buildDate$
                """.trimIndent())
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val throwable = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
                throwable,
                """
                usage: java -jar alexandria-app.jar
                       about [-h]
                
                Show version number and build date.
                
                named arguments:
                  -h, --help             show this help message and exit
                """.trimIndent())
    }

    companion object {
        private val command = AboutCommand().name
    }
}
