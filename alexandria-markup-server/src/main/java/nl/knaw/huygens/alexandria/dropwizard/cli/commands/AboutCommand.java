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

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.dropwizard.cli.CLIContext;
import nl.knaw.huygens.alexandria.dropwizard.cli.DocumentInfo;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.storage.TAGDocument;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;

import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

public class AboutCommand extends AlexandriaCommand {
  private AppInfo appInfo;

  public AboutCommand() {
    super("about", "Show info about the registered documents and views.");
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
    checkAlexandriaIsInitialized();

    CLIContext context = readContext();
    try (TAGStore store = getTAGStore()) {
      showDocuments(store, context);
      showViews(store, context);
    }
  }

  private void showDocuments(final TAGStore store, final CLIContext context) {
    String documents = context.getDocumentInfo()
        .keySet()
        .stream()
        .sorted()
        .map(docName -> docInfo(docName, context.getDocumentInfo().get(docName), store))
        .collect(joining("\n  "));
    if (documents.isEmpty()) {
      System.out.println("no documents");
    } else {
      System.out.printf("Documents:%n  %s%n%n", documents);
    }
  }

  private String docInfo(final String docName, final DocumentInfo documentInfo, final TAGStore store) {
    Long docId = documentInfo.getDbId();
    String sourceFile = documentInfo.getSourceFile();
    return store.runInTransaction(() -> {
      TAGDocument document = store.getDocument(docId);
      return format("%s%n    created:  %s%n    modified: %s%n    source: %s",
          docName,
          document.getCreationDate(),
          document.getModificationDate(),
          sourceFile
      );
    });
  }

  private void showViews(final TAGStore store, final CLIContext context) {
    String views = readViewMap(store, context)
        .entrySet()
        .stream()
        .map(this::toString)
        .collect(joining("\n  "));
    if (views.isEmpty()) {
      System.out.println("no views");
    } else {
      System.out.printf("Views:%n  %s%n%n", views);
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

