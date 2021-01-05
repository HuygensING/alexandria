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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.SPARQLQueryCommand
import org.junit.Test

class SPARQLQueryCommandIntegrationTest : CommandIntegrationTest() {

    @Test
    @Throws(Exception::class)
    fun testCommand() {
        runInitCommand()

        // create sourcefile
        val tagFilename = createTagmlFileName("transcription")
        val tagml = "[tagml>[l>test [w>word<w]<l]<tagml]"
        val tagPath = createFile(tagFilename, tagml)

        // create sourcefile
        val queryFilename = "query.sparql"
        val sparql = ("prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "prefix tag: <https://huygensing.github.io/TAG/TAGML/ontology/tagml.ttl#> "
                + "select ?markup (count(?markup) as ?count) "
                + "where { [] tag:markup_name ?markup . } "
                + "group by ?markup "
                +  // otherwise: "Non-group key variable in SELECT"
                "order by ?markup")
        createFile(queryFilename, sparql)
        runAddCommand(tagPath)
        runCommitAllCommand()
        val success = cli!!.run(command, "transcription", "-q", "query.sparql")
        val expectedOutput = """
            %document: transcription
            %
            %query:
            %  prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> prefix tag: <https://huygensing.github.io/TAG/TAGML/ontology/tagml.ttl#> select ?markup (count(?markup) as ?count) where { [] tag:markup_name ?markup . } group by ?markup order by ?markup
            %
            %result:
            %-------------------
            %| markup  | count |
            %===================
            %| "l"     | 1     |
            %| "tagml" | 1     |
            %| "w"     | 1     |
            %-------------------
            %""".trimMargin("%")
        //    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
        assertSucceedsWithExpectedStdout(success, expectedOutput)
    }

    @Test
    @Throws(Exception::class)
    fun testCommandHelp() {
        val success = cli!!.run(command, "-h")
        assertSucceedsWithExpectedStdout(
            success,
            """
            |usage: java -jar alexandria-app.jar
            |       query -q <sparql-file> [-h] <document>
            |
            |Query the document using SPARQL.
            |
            |positional arguments:
            |  <document>             The name of the document to query.
            |
            |named arguments:
            |  -q <sparql-file>, --query <sparql-file>
            |                         The file containing the SPARQL query.
            |  -h, --help             show this help message and exit
            |""".trimMargin()
        )
    }

    companion object {
        private val command = SPARQLQueryCommand().name
    }
}
