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

import com.google.common.base.Charsets;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.view.TAGView;
import nl.knaw.huygens.alexandria.view.TAGViewFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Map;

public class DefineViewCommand extends AlexandriaCommand {

  public DefineViewCommand() {
    super("define-view", "Read in a view definition");
  }

  @Override
  public void configure(Subparser subparser) {
    subparser.addArgument("-n", "--name")//
        .dest(NAME)//
        .type(String.class)//
        .required(true)//
        .help("The name of the view");

    subparser.addArgument("-f", "--file")//
        .dest(FILE)//
        .type(String.class)//
        .required(true)//
        .help("The file containing the view definition");
  }

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
    checkDirectoryIsInitialized();
    System.out.printf("Parsing %s to view %s...%n", namespace.getString(FILE), namespace.getString(NAME));
    Map<String, TAGView> viewMap = readViewMap();
    String filename = namespace.getString(FILE);
    File viewFile = new File(filename);
    try (TAGStore store = getTAGStore()) {
      TAGViewFactory viewFactory = new TAGViewFactory(store);
      String json = FileUtils.readFileToString(viewFile, Charsets.UTF_8);
      TAGView view = viewFactory.fromJsonString(json);
      String viewName = namespace.getString(NAME);
      viewMap.put(viewName, view);
      storeViewMap(viewMap);
    }
    System.out.println("done!");
  }

}
