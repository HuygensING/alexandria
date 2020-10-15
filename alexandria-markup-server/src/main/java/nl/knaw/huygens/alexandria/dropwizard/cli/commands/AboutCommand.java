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

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;

public class AboutCommand extends AlexandriaCommand {
  private AppInfo appInfo;

  public AboutCommand() {
    super("about", "Show version number and build date.");
  }

  public Command withAppInfo(final AppInfo appInfo) {
    this.appInfo = appInfo;
    return this;
  }

  @Override
  public void configure(Subparser subparser) {}

  @Override
  public void run(Bootstrap<?> bootstrap, Namespace namespace) {
    System.out.printf("Alexandria version %s%n", appInfo.getVersion());
    System.out.printf("Build date: %s%n%n", appInfo.getBuildDate());
  }
}
