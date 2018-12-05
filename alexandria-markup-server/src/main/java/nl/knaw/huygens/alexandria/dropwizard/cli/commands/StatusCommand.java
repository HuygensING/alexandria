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

import com.google.common.collect.Multimap;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;

import java.io.IOException;

public class StatusCommand extends AlexandriaCommand {

  public StatusCommand() {
    super("status", "Show the directory status (active view, modified files, etc.).");
  }

  @Override
  public void configure(Subparser subparser) {
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkDirectoryIsInitialized();

    CLIContext context = readContext();
    System.out.printf("Active view: %s%n%n", context.getActiveView());
    showChanges(context);
  }

  private void showChanges(final CLIContext context) throws IOException {
    Multimap<FileStatus, String> fileStatusMap = readWorkDirStatus(context);
    showChanges(fileStatusMap);
  }


}

