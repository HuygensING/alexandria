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

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import static java.util.Comparator.comparing;

public class HelpCommand extends AlexandriaCommand {
  public HelpCommand() {
    super("help", "Show the available commands and their descriptions.");
  }

  @Override
  public void configure(Subparser subparser) {

  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    System.out.println("usage: alexandria [-h] <command> [<args>]\n" +
        "\n" +
        "Available commands:\n");
    bootstrap.getCommands().stream()
        .sorted(comparing(Command::getName))
        .map(this::toCommandHelpLine)
        .forEach(System.out::println);
  }

  private String toCommandHelpLine(final Command command) {
    String commandName = command.getName();
    return String.format("%-12s- %s", commandName, command.getDescription());
  }
}
