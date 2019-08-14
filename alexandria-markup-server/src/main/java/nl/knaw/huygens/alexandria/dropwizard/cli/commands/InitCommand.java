package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
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

import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InitCommand extends AlexandriaCommand {

  public InitCommand() {
    super("init", "Initializes current directory as an alexandria workspace.");
  }

  @Override
  public void configure(Subparser subparser) {

  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkWeAreNotInUserHomeDir();
    CLIContext context = new CLIContext();
    initPaths(Paths.get("").toAbsolutePath());
    context.getWatchedDirectories().add("");
    System.out.println("initializing...");
    System.out.println("  mkdir " + Paths.get(alexandriaDir));

    new File(alexandriaDir).mkdir();
    Path transcriptionsPath = Paths.get(workDir, SOURCE_DIR);
    System.out.println("  mkdir " + transcriptionsPath);
    mkdir(transcriptionsPath);
    context.getWatchedDirectories().add(SOURCE_DIR);

    Path viewsPath = Paths.get(workDir, VIEWS_DIR);
    System.out.println("  mkdir " + viewsPath);
    mkdir(viewsPath);
    context.getWatchedDirectories().add(VIEWS_DIR);

    storeContext(context);

    System.out.println("done!");
  }

  private void checkWeAreNotInUserHomeDir() {
    String homeDir = System.getProperty("user.home");
    String currentPath = Paths.get("").toAbsolutePath().toString();
    if (homeDir.equals(currentPath)) {
      throw new AlexandriaCommandException("You are currently in your home directory, which can't be used as an alexandria directory. Please choose a different directory to initialize.");
    }
  }

  private void mkdir(final Path path) throws IOException {
    if (!Files.exists(path)) {
      Files.createDirectory(path);
    }
  }
}
