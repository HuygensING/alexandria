package nl.knaw.huygens.alexandria.dropwizard;

import org.hibernate.validator.constraints.NotEmpty;

import io.dropwizard.Configuration;

public class ServerConfiguration extends Configuration {
  @NotEmpty
  private String baseURI;

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  public String getBaseURI() {
    return baseURI;
  }

}
