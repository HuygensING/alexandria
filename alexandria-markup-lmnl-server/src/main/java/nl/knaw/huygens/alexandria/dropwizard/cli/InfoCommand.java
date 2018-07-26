package nl.knaw.huygens.alexandria.dropwizard.cli;

/*
 * #%L
 * alexandria-markup-lmnl-server
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
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.view.TAGView;

import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class InfoCommand extends AlexandriaCommand {
  private AppInfo appInfo;

  public InfoCommand() {
    super("info", "Show info about the alexandria graph.");
  }

  public Command withAppInfo(final AppInfo appInfo) {
    this.appInfo = appInfo;
    return this;
  }

  @Override
  public void configure(Subparser subparser) {
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    System.out.printf("alexandria version %s%n", appInfo.getVersion());
    System.out.printf("build date: %s%n%n", appInfo.getBuildDate());
    showDocuments();
    showViews();
  }

  private void showDocuments() {
    Map<String, Long> documentIndex = readDocumentIndex();
    String documents = documentIndex
        .keySet()
        .stream()
        .sorted()
        .map(docName -> docInfo(docName, documentIndex))
        .collect(joining("\n  "));
    if (documents.isEmpty()) {
      System.out.println("no documents");
    } else {
      System.out.printf("documents:%n  %s%n%n", documents);
    }
  }

  private String docInfo(final String docName, final Map<String, Long> documentIndex) {
    Long docId = documentIndex.get(docName);
    store.open();
    String docInfo = store.runInTransaction(() -> {
      TAGDocument document = store.getDocument(docId);
      return format("%s (created:%s, modified:%s)", docName, document.getCreationDate(), document.getModificationDate());
    });
    store.close();
    return docInfo;
  }

  private void showViews() {
    String views = readViewMap()
        .entrySet()
        .stream()
        .map(this::toString)
        .collect(joining("\n  "));
    if (views.isEmpty()) {
      System.out.println("no views");
    } else {
      System.out.printf("views:%n  %s%n%n", views);
    }
  }

  private String toString(Map.Entry<String, TAGView> entry) {
    String k = entry.getKey();
    TAGView v = entry.getValue();
    String inOrEx;
    Set<String> relevantMarkup;
    if (v.markupStyleIsInclude()) {
      inOrEx = "included";
      relevantMarkup = v.getMarkupToInclude();

    } else {
      inOrEx = "excluded";
      relevantMarkup = v.getMarkupToExclude();
    }
    String markup = relevantMarkup.stream()
        .sorted()
        .collect(joining(" "));
    return format("%s: %s markup = %s", k, inOrEx, markup);
  }
}
