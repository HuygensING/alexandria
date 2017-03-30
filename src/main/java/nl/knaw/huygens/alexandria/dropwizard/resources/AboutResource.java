package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.time.Instant;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import nl.knaw.huygens.alexandria.dropwizard.api.AboutInfo;
import nl.knaw.huygens.alexandria.dropwizard.api.RootPaths;

@Path(RootPaths.ABOUT)
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {

  AboutInfo about = new AboutInfo();

  public AboutResource() {
    this("appName");
  }

  public AboutResource(String appName) {
    about.setAppName(appName);
    about.setStartedAt(Instant.now().toString());
    about.setVersion("version");
  }

  @GET
  @Timed
  public AboutInfo getAbout() {
    return about;
  }
}
