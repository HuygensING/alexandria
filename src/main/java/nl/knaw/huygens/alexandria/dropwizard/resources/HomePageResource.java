package nl.knaw.huygens.alexandria.dropwizard.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

@Path("/")
public class HomePageResource {

  /**
   * Shows the homepage for the backend
   *
   * @return HTML representation of the homepage
   * @throws IOException
   */

  @GET
  @Timed
  @Produces(MediaType.TEXT_HTML)
  public Response getHomePage() throws IOException {
    InputStream resourceAsStream = Thread.currentThread()//
        .getContextClassLoader().getResourceAsStream("index.html");
    return Response//
        .ok(resourceAsStream)//
        .header("Pragma", "public")//
        .header("Cache-Control", "public")//
        .build();

  }

  @GET
  @Path("favicon.ico")
  public Response getFavIcon() {
    return Response.noContent().build();
  }

  @GET
  @Path("robots.txt")
  public String noRobots() {
    return "User-agent: *\nDisallow: /\n";
  }
}
