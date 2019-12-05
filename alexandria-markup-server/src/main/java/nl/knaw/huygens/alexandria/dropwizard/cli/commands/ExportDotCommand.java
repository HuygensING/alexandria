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

import com.google.common.base.Charsets;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ExportDotCommand extends AbstractGraphvizCommand {

  public ExportDotCommand() {
    super("export-dot", "Export the document as .dot file.");
  }

  @Override
  protected String getFormat() {
    return "dot";
  }

  @Override
  protected void renderToFile(final String dot, final String fileName) throws IOException {
    FileUtils.writeStringToFile(new File(fileName), dot, Charsets.UTF_8);
  }

  @Override
  protected void renderToStdOut(final String dot) {
    System.out.println(dot);
  }
}
