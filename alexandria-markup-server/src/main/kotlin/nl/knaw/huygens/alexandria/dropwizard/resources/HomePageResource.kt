package nl.knaw.huygens.alexandria.dropwizard.resources


/*
* #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2020 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * #L%
*/

import com.codahale.metrics.annotation.Timed
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Api("/")
@Path("/")
class HomePageResource {
    /**
     * Shows the homepage for the backend
     *
     * @return HTML representation of the homepage
     */
    @get:ApiOperation(value = "Show the server homepage")
    @get:Timed
    @get:GET
    @get:Produces(MediaType.TEXT_HTML)
    val homePage: Response
        get() {
            val resourceAsStream = Thread.currentThread().contextClassLoader.getResourceAsStream("index.html")
            return Response.ok(resourceAsStream)
                    .header("Pragma", "public")
                    .header("Cache-Control", "public")
                    .build()
        }

    @get:ApiOperation(value = "Placeholder for favicon.ico")
    @get:GET
    @get:Path("favicon.ico")
    val favIcon: Response
        get() = Response.noContent().build()

    @GET
    @Path("robots.txt")
    @ApiOperation(value = "Placeholder for robots.txt")
    fun noRobots(): String = "User-agent: *\nDisallow: /\n"
}
