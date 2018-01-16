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

import static java.util.stream.Collectors.toMap;

public abstract class AlexandriaCommand extends Command {
  private static final Logger LOG = LoggerFactory.getLogger(AlexandriaCommand.class);
  static final String PROJECT_DIR = ".alexandria";
  String NAME = "name";
  String FILE = "file";
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

  File viewsFile = new File(PROJECT_DIR, "views.json");

  Map<String, TAGView> readViewMap() {
    TAGViewFactory viewFactory = new TAGViewFactory(store);
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      TypeReference<HashMap<String, TAGViewDefinition>> typeReference = new TypeReference<HashMap<String, TAGViewDefinition>>() {
      };
      Map<String, TAGViewDefinition> stringTAGViewMap = objectMapper.readValue(viewsFile, typeReference);
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
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      objectMapper.writeValue(viewsFile, viewDefinitionMap);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
