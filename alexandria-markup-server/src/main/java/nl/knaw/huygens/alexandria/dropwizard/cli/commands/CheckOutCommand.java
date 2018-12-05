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
import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException;

import java.io.IOException;
import java.util.Collection;

public class CheckOutCommand extends AlexandriaCommand {
  private static final String VIEW = "view";
  private static final String DOCUMENT = "document";

  public CheckOutCommand() {
    super("checkout", "Activate or deactivate a view in this directory.");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("view")//
        .metavar("<view>")
        .dest(VIEW)//
        .type(String.class)//
        .required(true)//
        .help("The name of the view to use");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkDirectoryIsInitialized();
    checkDirectoryHasNoUnCommittedChanges();

    String viewName = namespace.getString(VIEW);
    checkoutView(viewName);
    System.out.println("done!");
  }

  private void checkDirectoryHasNoUnCommittedChanges() throws IOException {
    Multimap<FileStatus, String> fileStatus = readWorkDirStatus(readContext());
    Collection<String> changedFiles = fileStatus.get(FileStatus.changed);
    Collection<String> deletedFiles = fileStatus.get(FileStatus.deleted);
    if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
      showChanges(fileStatus);
      String message = "Uncommitted changes found, cannot checkout another view.";
      throw new AlexandriaCommandException(message);
    }
  }

}

