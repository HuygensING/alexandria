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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.ExportDotCommand;
import org.junit.Test;

public class ExportDotCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new ExportDotCommand().getName();

  @Test
  public void testCommand() throws Exception {
    runInitCommand();

    String tagFilename = "transcriptions/transcription.tagml";
    String tagml = "[tagml>[l>test<l]<tagml]";
    createFile(tagFilename, tagml);

    runAddCommand(tagFilename);
    runCommitAllCommand();

    boolean success = cli.run(command, "-d", "transcription");
    softlyAssertSucceedsWithExpectedStdout(success, "digraph TextGraph{\n" +
        "  node [font=\"helvetica\";style=\"filled\";fillcolor=\"white\"]\n" +
        "  d [shape=doublecircle;label=\"\"]\n" +
        "  subgraph{\n" +
        "    t4 [shape=box;arrowhead=none;label=<#PCDATA<br/>test>]\n" +
        "    rank=same\n" +
        "  }\n" +
        "  m2 [color=red;label=<tagml>]\n" +
        "  m3 [color=red;label=<l>]\n" +
        "  m2->m3[color=red;arrowhead=none]\n" +
        "  m3->t4[color=red;arrowhead=none]\n" +
        "  d->m2 [arrowhead=none]\n" +
        "}");
  }

  @Test
  public void testCommandHelp() throws Exception {
    final boolean success = cli.run(command, "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       export-dot -d DOCUMENT [-h]\n" +
        "\n" +
        "Export the document as .dot file.\n" +
        "\n" +
        "named arguments:\n" +
        "  -d DOCUMENT, --document DOCUMENT\n" +
        "                         The name of the document to export.\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommandShouldBeRunInAnInitializedDirectory() throws Exception {
    assertCommandRunsInAnInitializedDirectory(command, "-d", "document");
  }
}
