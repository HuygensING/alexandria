package nl.knaw.huygens.alexandria.dropwizard;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.dropwizard.health.ServerHealthCheck;
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.HomePageResource;
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;

public class ServerApplication extends Application<ServerConfiguration> {

  public static void main(String[] args) throws Exception {
    new ServerApplication().run(args);
  }

  @Override
  public String getName() {
    return "Alexandria Markup Server";
  }

  @Override
  public void initialize(Bootstrap<ServerConfiguration> bootstrap) {
  }

  @Override
  public void run(ServerConfiguration configuration, Environment environment) {
    DocumentService documentService = new DocumentService(configuration);
    LMNLImporter lmnlImporter = new LMNLImporter();
    LMNLExporter lmnlExporter = new LMNLExporter();

    environment.jersey().register(new HomePageResource());
    environment.jersey().register(new AboutResource(getName()));
    environment.jersey().register(new DocumentsResource(documentService, lmnlImporter, lmnlExporter, configuration));

    environment.healthChecks().register("server", new ServerHealthCheck());
  }
}
