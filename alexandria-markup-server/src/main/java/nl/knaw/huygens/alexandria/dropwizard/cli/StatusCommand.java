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

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.view.TAGView;

import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class StatusCommand extends AlexandriaCommand {
  private AppInfo appInfo;

  public StatusCommand() {
    super("status", "Show info about the alexandria graph and the directory status.");
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
    System.out.printf("Alexandria version %s%n", appInfo.getVersion());
    System.out.printf("Build date: %s%n%n", appInfo.getBuildDate());
    checkDirectoryIsInitialized();

    CLIContext context = readContext();
    System.out.printf("Active view: %s%n", context.getActiveView());
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

    List<String> info = new ArrayList<>();

    String markupRelevance = "";
    Set<String> relevantMarkup = new TreeSet<>();
    if (v.markupStyleIsInclude()) {
      markupRelevance = "included";
      relevantMarkup.addAll(v.getMarkupToInclude());

    } else if (v.markupStyleIsExclude()) {
      markupRelevance = "excluded";
      relevantMarkup.addAll(v.getMarkupToExclude());
    }
    if (!relevantMarkup.isEmpty()) {
      String markup = relevantMarkup.stream()
          .sorted()
          .collect(joining(" "));
      String markupInfo = format("%s markup = %s", markupRelevance, markup);
      info.add(markupInfo);
    }

    String layerRelevance = "";
    Set<String> relevantLayers = new TreeSet<>();
    if (v.layerStyleIsInclude()) {
      layerRelevance = "included";
      relevantLayers.addAll(v.getLayersToInclude());

    } else if (v.layerStyleIsExclude()) {
      layerRelevance = "excluded";
      relevantLayers.addAll(v.getLayersToExclude());
    }
    if (!relevantLayers.isEmpty()) {
      String layers = relevantLayers.stream()
          .sorted()
          .collect(joining(" "));
      String layerInfo = format("%s layers = %s", layerRelevance, layers);
      info.add(layerInfo);
    }
    return format("%s:\n    %s", k, String.join("\n    ", info));
  }
}