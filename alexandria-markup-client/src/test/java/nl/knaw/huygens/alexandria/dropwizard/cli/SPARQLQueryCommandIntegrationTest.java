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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.SPARQLQueryCommand;
import org.junit.Test;

public class SPARQLQueryCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new SPARQLQueryCommand().getName();

//  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    // create sourcefile
    String tagFilename = createTagmlFileName("transcription");
    String tagml = "[tagml>[l>test [w>word<w]<l]<tagml]";
    createFile(tagFilename, tagml);

    // create sourcefile
    String queryFilename = "query.sparql";
    String sparql = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "prefix tag: <https://huygensing.github.io/TAG/TAGML/ontology/tagml.ttl#> " +
        "select ?markup (count(?markup) as ?count) " +
        "where { [] tag:markup_name ?markup . } " +
        "group by ?markup " + // otherwise: "Non-group key variable in SELECT"
        "order by ?markup";
    createFile(queryFilename, sparql);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    final boolean success = cli.run(command, "-d", "transcription", "-q", "query.sparql");
    String expectedOutput = "document: transcription\n" +
        "query:\n" +
        "  prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
        " prefix tag: <https://huygensing.github.io/TAG/TAGML/ontology/tagml.ttl#>" +
        " select ?markup (count(?markup) as ?count)" +
        " where { [] tag:markup_name ?markup . }" +
        " group by ?markup" +
        " order by ?markup\n" +
        "result:\n" +
        "-------------------\n" +
        "| markup  | count |\n" +
        "===================\n" +
        "| \"l\"     | 1     |\n" +
        "| \"tagml\" | 1     |\n" +
        "| \"w\"     | 1     |\n" +
        "-------------------";
//    softlyAssertSucceedsWithExpectedStdout(success, expectedOutput);
    assertSucceedsWithExpectedStdout(success, expectedOutput);
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       query -d DOCUMENT -q QUERY [-h]\n" +
        "\n" +
        "Query the document using SPARQL.\n" +
        "\n" +
        "named arguments:\n" +
        "  -d DOCUMENT, --document DOCUMENT\n" +
        "                         The name of the document to query.\n" +
        "  -q QUERY, --query QUERY\n" +
        "                         The file containing the SPARQL query.\n" +
        "  -h, --help             show this help message and exit");
  }

}
