package nl.knaw.huygens.alexandria.markup.client;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource;
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;

public abstract class AlexandriaTestWithTestMarkupServer {

  private static final String BASEURI = "http://localhost:2017/";
  protected static URI testURI = URI.create(BASEURI);
  private static HttpServer testServer;

  @BeforeClass
  public static void startTestServer() {
    ServerConfiguration config = new ServerConfiguration();
    config.setBaseURI(BASEURI);

    ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(new AboutResource("appName"));
    resourceConfig.register(new DocumentsResource(new DocumentService(config), new LMNLImporter(), new LMNLExporter(), config));

    testServer = GrizzlyHttpServerFactory.createHttpServer(testURI, resourceConfig, true);
  }

  @AfterClass
  public static void stopTestServer() {
    testServer.shutdown();
  }

  static String prettyFormat(String input, int indent) {
    try {
      Source xmlInput = new StreamSource(new StringReader(input));
      StringWriter stringWriter = new StringWriter();
      StreamResult xmlOutput = new StreamResult(stringWriter);
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      // transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.transform(xmlInput, xmlOutput);
      return xmlOutput.getWriter().toString();
    } catch (Exception e) {
      throw new RuntimeException(e); // simple exception handling, please review it
    }
  }

  static String prettyFormat(String input) {
    return prettyFormat(input, 2);
  }
}
