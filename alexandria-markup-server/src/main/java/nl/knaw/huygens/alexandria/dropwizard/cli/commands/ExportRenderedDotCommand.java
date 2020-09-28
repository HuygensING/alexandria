package nl.knaw.huygens.alexandria.dropwizard.cli.commands;

/*
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.dropwizard.cli.AlexandriaCommandException;
import nl.knaw.huygens.graphviz.DotEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportRenderedDotCommand extends AbstractGraphvizCommand {
  private final String format;

  public ExportRenderedDotCommand(String format) {
    super(
        "export-" + format,
        "Export the document as " + format + ". (Requires access to Graphviz' dot command)");
    this.format = format;
  }

  @Override
  protected String getFormat() {
    return format;
  }

  @Override
  protected void renderToFile(final String dot, final String fileName) {
    DotEngine dotEngine = setupDotEngine();
    File file = new File(fileName);
    try (FileOutputStream fos = new FileOutputStream(file)) {
      file.createNewFile();
      dotEngine.renderAs(format, dot, fos);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void renderToStdOut(String dot) {
    DotEngine dotEngine = setupDotEngine();
    dotEngine.renderAs(format, dot, System.out);
  }

  private DotEngine setupDotEngine() {
    DotEngine dotEngine = new DotEngine();
    String dotVersion = dotEngine.getDotVersion();
    if (dotVersion.isEmpty()) {
      throw new AlexandriaCommandException(
          "This command needs access to the Graphviz dot command, which was not found.\n"
              + "See https://www.graphviz.org/ for installation instructions.");
    }
    return dotEngine;
  }
}
