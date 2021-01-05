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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.HelpCommand

class HelpCommandIntegrationTest : CommandIntegrationTest() {
    //  @Test
    @Throws(Exception::class)
    fun testHelpCommand() {
        val success = cli!!.run(command)
        softlyAssertSucceedsWithExpectedStdout(
                success,
                """
                    usage: alexandria [-h] <command> [<args>]
                    
                    Available commands:
                    
                    about       - Show info about the registered documents and views.
                    add         - Add file context to the index.
                    checkout    - Activate or deactivate a view in this directory.
                    commit      - Record changes to the repository.
                    diff        - Show the changes made to the file.
                    export-dot  - Export the document as .dot file.
                    export-png  - Export the document as png.
                    export-svg  - Export the document as svg.
                    export-xml  - Export the document as xml.
                    help        - Show the available commands and their descriptions.
                    init        - Initializes current directory as an alexandria workspace.
                    query       - Query the document using SPARQL.
                    revert      - Restore the document file(s).
                    status      - Show the directory status (active view, modified files, etc.).
                    """.trimIndent())
    }

    companion object {
        private val command = HelpCommand().name
    }
}
