package nl.knaw.huygens.alexandria.dropwizard.resources;

/*
 * #%L
 * alexandria-markup-lmnl-server
 * =======
 * Copyright (C) 2015 - 2017 Huygens ING (KNAW)
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

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
