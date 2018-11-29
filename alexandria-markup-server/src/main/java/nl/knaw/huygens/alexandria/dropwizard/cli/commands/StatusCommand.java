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
import org.fusesource.jansi.AnsiConsole;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

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
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws IOException {
    checkDirectoryIsInitialized();

    CLIContext context = readContext();
    System.out.printf("Active view: %s%n%n", context.getActiveView());
    showChanges(context);
  }

  private void showChanges(final CLIContext context) throws IOException {
    Multimap<FileStatus, String> fileStatusMap = readWorkDirStatus(context);

    AnsiConsole.systemInstall();

    Set<String> changedFiles = new HashSet<>(fileStatusMap.get(FileStatus.changed));
    Set<String> deletedFiles = new HashSet<>(fileStatusMap.get(FileStatus.deleted));
    if (!(changedFiles.isEmpty() && deletedFiles.isEmpty())) {
      System.out.printf("Uncommitted changes:%n" +
          "  (use \"alexandria commit <file>...\" to commit the selected changes)%n" +
          "  (use \"alexandria commit -a\" to commit all changes)%n" +
          "  (use \"alexandria revert <file>...\" to discard changes)%n%n");
      Set<String> changedOrDeletedFiles = new TreeSet<>();
      changedOrDeletedFiles.addAll(changedFiles);
      changedOrDeletedFiles.addAll(deletedFiles);
      changedOrDeletedFiles.forEach(file -> {
            String status = changedFiles.contains(file)
                ? "        modified: "
                : "        deleted:  ";
            System.out.println(ansi().fg(RED).a(status).a(file).reset());
          }
      );
    }

    Collection<String> createdFiles = fileStatusMap.get(FileStatus.created);
    if (!createdFiles.isEmpty()) {
      System.out.printf("Untracked files:%n" +
          "  (use \"alexandria add <file>...\" to start tracking this file.)%n%n");
      createdFiles.stream().sorted().forEach(f ->
          System.out.println(ansi().fg(RED).a("        ").a(f).reset())
      );
    }

    AnsiConsole.systemUninstall();
  }

}

