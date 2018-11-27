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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class InitCommand extends AlexandriaCommand {

  public InitCommand() {
    super("init", "Initializes current directory as an alexandria workspace");
  }

  @Override
  public void configure(Subparser subparser) {

  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    catchExceptions(() -> {
      System.out.println("initializing...");

      try {
        Files.createDirectory(Paths.get(workDir, "transcriptions"));
        Files.createDirectory(Paths.get(workDir, "views"));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      CLIContext context = new CLIContext();
      storeContext(context);

      Map<String, Long> documentIndex = new HashMap<>();
      storeDocumentIndex(documentIndex);

      System.out.println("done!");
    });
  }
}
