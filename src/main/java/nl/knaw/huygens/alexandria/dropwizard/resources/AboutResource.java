package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.time.Instant;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

@Path(RootPaths.ABOUT)
@Produces(MediaType.APPLICATION_JSON)
public class AboutResource {

  AboutInfo about = new AboutInfo();
  private Instant startedAt;

  public AboutResource(String appName) {
    about.setName(appName);
    startedAt = Instant.now();
  }

  @GET
  @Timed
  public AboutInfo getAbout() {
    return about;
  }

  class AboutInfo {
    String appName;

    public void setName(String appName) {
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
