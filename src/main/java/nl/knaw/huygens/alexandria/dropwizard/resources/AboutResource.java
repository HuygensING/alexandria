package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.time.Instant;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import nl.knaw.huygens.alexandria.dropwizard.api.RootPaths;

@Path(RootPaths.ABOUT)
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {

  AboutInfo about = new AboutInfo();
  private static Instant startedAt;

  public AboutResource(String appName) {
    about.setAppName(appName);
    startedAt = Instant.now();
  }

  @GET
  @Timed
  public AboutInfo getAbout() {
    return about;
  }

  public static class AboutInfo {
    String appName;

    public void setAppName(String appName) {
      this.appName = appName;
    }

    public String getAppName() {
      return appName;
    }

    public String getStartedAt() {
      return startedAt.toString();
    }
  }
}
