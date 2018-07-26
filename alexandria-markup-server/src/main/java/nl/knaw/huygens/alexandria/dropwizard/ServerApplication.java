package nl.knaw.huygens.alexandria.dropwizard;

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

import com.codahale.metrics.health.HealthCheck.Result;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huc.di.tag.tagml.exporter.TAGMLExporter;
import nl.knaw.huc.di.tag.tagml.importer.TAGMLImporter;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.dropwizard.api.PropertiesConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.cli.*;
import nl.knaw.huygens.alexandria.dropwizard.health.ServerHealthCheck;
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.HomePageResource;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.SortedMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerApplication extends Application<ServerConfiguration> {
  private static final String PROPERTIES_FILE = "about.properties";
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  private AppInfo appInfo = getAppInfo();

  public static void main(String[] args) throws Exception {
    new ServerApplication().run(args);
  }

  private AppInfo getAppInfo() {
    final PropertiesConfiguration properties = new PropertiesConfiguration(PROPERTIES_FILE, true);
    return new AppInfo()
        .setAppName(getName())
        .setStartedAt(Instant.now().toString())
        .setBuildDate(properties.getProperty("buildDate").get())
        .setCommitId(properties.getProperty("commitId").get())
        .setScmBranch(properties.getProperty("scmBranch").get())
        .setVersion(properties.getProperty("version").get());
  }

  @Override
  public String getName() {
    return "Alexandria Markup Server";
  }

  @Override
  public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
    // Enable variable substitution with environment variables
    bootstrap.setConfigurationSourceProvider(//
        new SubstitutingSourceProvider(//
            bootstrap.getConfigurationSourceProvider(), //
            new EnvironmentVariableSubstitutor()));
    bootstrap.addBundle(new SwaggerBundle<ServerConfiguration>() {
      @Override
      protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ServerConfiguration configuration) {
        return configuration.swaggerBundleConfiguration;
      }
    });
    bootstrap.addCommand(new InitCommand());
    bootstrap.addCommand(new InfoCommand().withAppInfo(appInfo));
    bootstrap.addCommand(new RegisterDocumentCommand());
    bootstrap.addCommand(new QueryCommand());
    bootstrap.addCommand(new DefineViewCommand());
    bootstrap.addCommand(new CheckOutCommand());
    bootstrap.addCommand(new DiffCommand());
    bootstrap.addCommand(new RevertCommand());
    bootstrap.addCommand(new CheckInCommand());
  }

  @Override
  public void run(ServerConfiguration configuration, Environment environment) {
    DocumentService documentService = new DocumentService(configuration);
    TAGStore store = new TAGStore(configuration.getDbDir(), false);
    configuration.setStore(store);
    TAGMLImporter tagmlImporter = new TAGMLImporter(store);
    TAGMLExporter tagmlExporter = new TAGMLExporter(store);
    TexMECSImporter texMECSImporter = new TexMECSImporter(store);

    environment.jersey().register(new HomePageResource());
    environment.jersey().register(new AboutResource(appInfo));
    environment.jersey().register(new DocumentsResource(documentService, tagmlImporter, texMECSImporter, tagmlExporter, configuration));

    environment.healthChecks().register("server", new ServerHealthCheck());

    SortedMap<String, Result> results = environment.healthChecks().runHealthChecks();
    AtomicBoolean healthy = new AtomicBoolean(true);
    LOG.info("Healthchecks:");
    results.forEach((name, result) -> {
      LOG.info("{}: {}, message='{}'", name, result.isHealthy() ? "healthy" : "unhealthy", result.getMessage());
      healthy.set(healthy.get() && result.isHealthy());
    });
    if (!healthy.get()) {
      throw new RuntimeException("Failing health check(s)");
    }

  }
}
