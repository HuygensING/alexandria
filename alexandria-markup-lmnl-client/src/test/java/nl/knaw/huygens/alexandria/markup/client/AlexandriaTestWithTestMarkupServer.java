package nl.knaw.huygens.alexandria.markup.client;

/*
 * #%L
 * alexandria-markup-lmnl-client
 * =======
 * Copyright (C) 2015 - 2018 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.api.DocumentService;
import nl.knaw.huygens.alexandria.dropwizard.resources.AboutResource;
import nl.knaw.huygens.alexandria.dropwizard.resources.DocumentsResource;
import nl.knaw.huygens.alexandria.lmnl.exporter.LMNLExporter;
import nl.knaw.huygens.alexandria.lmnl.importer.LMNLImporter;
import nl.knaw.huygens.alexandria.storage.TAGStore;
import nl.knaw.huygens.alexandria.texmecs.importer.TexMECSImporter;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

public abstract class AlexandriaTestWithTestMarkupServer {
  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  private static final String BASEURI = "http://localhost:2017/";
  protected static URI testURI = URI.create(BASEURI);
  private static HttpServer testServer;

  @BeforeClass
  public static void startTestServer() throws IOException {
    ServerConfiguration config = new ServerConfiguration();
    config.setBaseURI(BASEURI);
    config.setStore(new TAGStore(tmpFolder.newFolder("db").getPath(), false));

    ResourceConfig resourceConfig = new ResourceConfig();
    resourceConfig.register(new AboutResource("appName"));
    TAGStore store = config.getStore();
    resourceConfig.register(new DocumentsResource(new DocumentService(config), new LMNLImporter(store), new TexMECSImporter(store), new LMNLExporter(store), config));

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
