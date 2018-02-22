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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.cli.Command;
import static java.util.stream.Collectors.toMap;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import nl.knaw.huygens.alexandria.view.TAGViewDefinition;
import nl.knaw.huygens.alexandria.view.TAGViewFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class AlexandriaCommand extends Command {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaCommand.class);
  static final String PROJECT_DIR = ".alexandria";
  final String NAME = "name";
  final String FILE = "file";
  final TAGStore store;

  public AlexandriaCommand(String name, String description) {
    super(name, description);
    initProjectDir();
    store = new TAGStore(PROJECT_DIR, false);
  }

  private void initProjectDir() {
    File dir = new File(PROJECT_DIR);
    dir.mkdir();
  }

  final File viewsFile = new File(PROJECT_DIR, "views.json");
  final File documentIndexFile = new File(PROJECT_DIR, "document_index.json");
  final File contextFile = new File(PROJECT_DIR, "context.json");

  Map<String, TAGView> readViewMap() {
    TAGViewFactory viewFactory = new TAGViewFactory(store);
    try {
      TypeReference<HashMap<String, TAGViewDefinition>> typeReference = new TypeReference<HashMap<String, TAGViewDefinition>>() {
      };
      Map<String, TAGViewDefinition> stringTAGViewMap = new ObjectMapper().readValue(viewsFile, typeReference);
      return stringTAGViewMap.entrySet()//
          .stream()//
          .collect(toMap(//
              Map.Entry::getKey,//
              e -> viewFactory.fromDefinition(e.getValue())//
          ));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void storeViewMap(Map<String, TAGView> viewMap) {
    Map<String, TAGViewDefinition> viewDefinitionMap = viewMap.entrySet()//
        .stream()//
        .collect(toMap(//
            Map.Entry::getKey,//
            e -> e.getValue().getDefinition()//
        ));
    try {
      new ObjectMapper().writeValue(viewsFile, viewDefinitionMap);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  Map<String, Long> readDocumentIndex() {
    try {
      TypeReference<HashMap<String, Long>> typeReference = new TypeReference<HashMap<String, Long>>() {
      };
      Map<String, Long> documentIndex = new ObjectMapper().readValue(documentIndexFile, typeReference);
      return documentIndex;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void storeDocumentIndex(Map<String, Long> documentIndex) {
    try {
      new ObjectMapper().writeValue(documentIndexFile, documentIndex);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  CLIContext readContext() {
    try {
      CLIContext context = new ObjectMapper().readValue(contextFile, CLIContext.class);
      return context;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void storeContext(CLIContext context) {
    try {
      new ObjectMapper().writeValue(contextFile, context);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


}
