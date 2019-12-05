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
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import nl.knaw.huygens.alexandria.dropwizard.cli.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

public class AddCommand extends AlexandriaCommand {

  public AddCommand() {
    super("add", "Add file context to the index.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument(ARG_FILE)
        .metavar("<file>")
        .dest(FILE)
        .type(String.class)
        .nargs("+")
        .required(true)
        .help("the files to watch");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    checkAlexandriaIsInitialized();
    List<String> files = relativeFilePaths(namespace);

    CLIContext cliContext = readContext();
    Map<String, FileInfo> watchedFiles = cliContext.getWatchedFiles();
    for (String file : files) {
      Path filePath = workFilePath(file);
      if (filePath.toFile().exists()) {
        try {
          if (Files.isRegularFile(filePath)) {
            Instant lastModifiedInstant = Files.getLastModifiedTime(filePath).toInstant();
            Instant lastCommit = lastModifiedInstant.minus(365L, DAYS); // set lastCommit to instant sooner than lastModifiedInstant
            FileInfo fileInfo = new FileInfo().setLastCommit(lastCommit);
            watchedFiles.put(file, fileInfo);
          } else if (Files.isDirectory(filePath)) {
            cliContext.getWatchedDirectories().add(file);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        System.err.printf("%s is not a file!%n", file);
      }
    }
    storeContext(cliContext);
    System.out.println();
  }

}
