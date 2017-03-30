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

import java.time.Instant;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.annotation.Timed;

import nl.knaw.huygens.alexandria.markup.api.AboutInfo;
import nl.knaw.huygens.alexandria.markup.api.ResourcePaths;

@Path(ResourcePaths.ABOUT)
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
