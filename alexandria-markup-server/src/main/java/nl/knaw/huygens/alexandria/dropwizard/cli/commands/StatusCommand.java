package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import org.fusesource.jansi.AnsiConsole;

import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.ansi;

public class StatusCommand extends AlexandriaCommand {

  public StatusCommand() {
    super("status", "Show the directory status (active view, modified files, etc.).");
  }

  @Override
  public void configure(Subparser subparser) {
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();

    CLIContext context = readContext();
    System.out.printf("Active view: %s%n%n", context.getActiveView());
    showChanges(context);
  }

  private void showChanges(final CLIContext context) {
    AnsiConsole.systemInstall();
    System.out.println("Changes not staged for commit:\n" +
        "  (use \"alexandria commit <file>...\" to commit the selected changes)\n" +
        "  (use \"alexandria commit -a\" to commit all changes)\n" +
        "  (use \"alexandria revert <file>...\" to discard changes)\n");
    System.out.println(ansi().fg(RED).a("    modified: ....").reset());
    AnsiConsole.systemUninstall();
  }

}

