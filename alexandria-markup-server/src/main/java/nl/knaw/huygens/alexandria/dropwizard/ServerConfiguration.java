package nl.knaw.huygens.alexandria.dropwizard;

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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import org.hibernate.validator.constraints.NotEmpty;

public class ServerConfiguration extends Configuration {
  @NotEmpty private String baseURI;
  private TAGStore store;
  private String dbDir;

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI.replaceFirst("/$", "");
  }

  public String getBaseURI() {
    return baseURI;
  }

  @JsonProperty("swagger")
  public SwaggerBundleConfiguration swaggerBundleConfiguration;

  public TAGStore getStore() {
    return store;
  }

  public void setStore(TAGStore store) {
    this.store = store;
  }

  public void setDbDir(String dbDir) {
    this.dbDir = dbDir;
  }

  public String getDbDir() {
    return dbDir;
  }
}
