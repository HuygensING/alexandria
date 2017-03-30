package nl.knaw.huygens.alexandria.dropwizard.api;

import javax.ws.rs.core.MediaType;

public class UTF8MediaType {
  private static final String ENCODING_UTF8 = ";encoding=UTF8";
  public static final String TEXT_PLAIN = MediaType.TEXT_PLAIN + ENCODING_UTF8;
}
