package nl.knaw.huygens.alexandria.markup.client;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import nl.knaw.huygens.alexandria.markup.api.AboutInfo;

public class OptimisticAlexandriaMarkupClient {
  AlexandriaMarkupClient delegate;

  // constructors

  public OptimisticAlexandriaMarkupClient(final URI alexandriaURI) {
    delegate = new AlexandriaMarkupClient(alexandriaURI);
  }

  public OptimisticAlexandriaMarkupClient(final String alexandriaURI) {
    this(URI.create(alexandriaURI));
  }

  public OptimisticAlexandriaMarkupClient(final URI alexandriaURI, SSLContext sslContext) {
    delegate = new AlexandriaMarkupClient(alexandriaURI, sslContext);
  }

  public OptimisticAlexandriaMarkupClient(final String alexandriaURI, SSLContext sslContext) {
    this(URI.create(alexandriaURI), sslContext);
  }

  // convenience methods

  // delegated methods

  public WebTarget getRootTarget() {
    return delegate.getRootTarget();
  }

  public void close() {
    delegate.close();
  }

  public void setProperty(String jerseyClientProperty, Object value) {
    delegate.setProperty(jerseyClientProperty, value);
  }

  public AboutInfo getAbout() {
    return unwrap(delegate.getAbout());
  }

  public UUID addDocument(String lmnl) {
    return unwrap(delegate.addDocument(lmnl));
  }

  public String getLMNL(UUID uuid) {
    return unwrap(delegate.getLMNL(uuid));
  }

  public void setDocument(UUID uuid, String string) {
    unwrap(delegate.setDocument(uuid, string));
  }

  public String getMarkupDepthLaTex(UUID uuid) {
    return unwrap(delegate.getMarkupDepthLaTex(uuid));
  }

  public String getDocumentLaTeX(UUID uuid) {
    return unwrap(delegate.getDocumentLaTeX(uuid));
  }

  public String getMatrixLaTex(UUID uuid) {
    return unwrap(delegate.getMatrixLaTex(uuid));
  }

  public String getKdTreeLaTex(UUID uuid) {
    return unwrap(delegate.getKdTreeLaTex(uuid));
  }

  /////// end delegated methods

  private <T> T unwrap(RestResult<T> restResult) {
    if (restResult.hasFailed()) {
      Optional<Response> response = restResult.getResponse();
      String status = response.isPresent() ? response.get().getStatus() + ": " : "";
      String message = status + restResult.getFailureCause().orElse("Unspecified error");
      throw new AlexandriaException(message);
    }
    return restResult.get();
  }

}
