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

import nl.knaw.huygens.alexandria.dropwizard.cli.commands.HelpCommand;
import org.junit.Test;

public class HelpCommandIntegrationTest extends CommandIntegrationTest {

  private static final String command = new HelpCommand().getName();

  @Test
  public void testHelpCommand() throws Exception {
    final boolean success = cli.run(command);
    softlyAssertSucceedsWithExpectedStdout(success, "usage: alexandria [-h] <command> [<args>]\n" +
        "\n" +
        "Available commands:\n" +
        "\n" +
        "about       - Show info about the registered documents and views.\n" +
        "add         - Add file context to the index.\n" +
        "checkout    - Activate or deactivate a view in this directory.\n" +
        "commit      - Record changes to the repository.\n" +
        "diff        - Show the changes made to the file.\n" +
        "export-dot  - Export the document as .dot file.\n" +
        "export-png  - Export the document as png.\n" +
        "export-svg  - Export the document as svg.\n" +
        "export-xml  - Export the document as xml.\n" +
        "help        - Show the available commands and their descriptions.\n" +
        "init        - Initializes current directory as an alexandria workspace.\n" +
        "revert      - Restore the document file(s).\n" +
        "status      - Show the directory status (active view, modified files, etc.).");
  }

}
