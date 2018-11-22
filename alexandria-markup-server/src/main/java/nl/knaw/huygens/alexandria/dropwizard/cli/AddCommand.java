package nl.knaw.huygens.alexandria.dropwizard.cli;

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

import java.util.List;

public class AddCommand extends AlexandriaCommand {

  public static final String ARG_FILE = "file";

  public AddCommand() {
    super("add", "Add file context to the index");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument(ARG_FILE)//
        .metavar("FILE")
        .dest(FILE)//
        .type(String.class)//
        .nargs("+")
        .required(true)//
        .help("the files to watch");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkDirectoryIsInitialized();
    List<String> files = namespace.getList(ARG_FILE);
    CLIContext cliContext = readContext();
    cliContext.getWatchedFiles().addAll(files);
    storeContext(cliContext);
    System.out.println("");
  }
}
