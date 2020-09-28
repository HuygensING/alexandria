package nl.knaw.huygens.alexandria.dropwizard.cli.commands

/*
* #%L
 * alexandria-markup-server
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

import io.dropwizard.cli.Command
import io.dropwizard.setup.Bootstrap
import net.sourceforge.argparse4j.inf.Namespace
import net.sourceforge.argparse4j.inf.Subparser
import java.util.*

class HelpCommand : AlexandriaCommand("help", "Show the available commands and their descriptions.") {
    override fun configure(subparser: Subparser) {}
    override fun run(bootstrap: Bootstrap<*>, namespace: Namespace) {
        println(
                """
                    usage: alexandria [-h] <command> [<args>]
                    
                    Available commands:
                    
                    """.trimIndent())
        bootstrap.commands.stream()
                .sorted(Comparator.comparing { obj: Command -> obj.name })
                .map { command: Command -> toCommandHelpLine(command) }
                .forEach { x: String? -> println(x) }
    }

    private fun toCommandHelpLine(command: Command): String {
        val commandName = command.name
        return String.format("%-12s- %s", commandName, command.description)
    }
}
