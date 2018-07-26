package nl.knaw.huygens.alexandria.markup.client;

/*
 * #%L
 * alexandria-markup-client
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

import com.fasterxml.jackson.databind.JsonNode;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import org.junit.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.UUID;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

public class OptimisticAlexandriaMarkupClientTest extends AlexandriaTestWithTestMarkupServer {
  // private final Logger LOG = LoggerFactory.getLogger(getClass());
  private static final String EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX = ".*\\.";
  private static OptimisticAlexandriaMarkupClient client;

  @BeforeClass
  public static void startClient() {
    client = new OptimisticAlexandriaMarkupClient("http://localhost:2017/");
  }

  @AfterClass
  public static void stopClient() {
    client.close();
  }

  @Before
  public void before() {
  }

  @Ignore
  @Test
  public void testAbout() {
    AppInfo about = client.getAbout();
    assertThat(about.getVersion()).isNotEmpty();
  }

  @Ignore
  @Test
  public void testOptimisticAlexandriaMarkupClientHasDelegatedUnwrappedMethodForEachRelevantMethodInAlexandriaMarkupClient() {
    Class<AlexandriaMarkupClient> a = AlexandriaMarkupClient.class;

    String stubs = Arrays.stream(a.getMethods())//
        .filter(this::returnsRestResult)//
        .filter(this::hasNoDelegatedMethodInOptimisticAlexandriaMarkupClient)//
        .map(this::toDelegatedMethodStub)//
        .collect(joining("\n"));
    // LOG.info("Methods to add to OptimisticAlexandriaMarkupClient:\n{}", stubs);
    assertThat(stubs).isEmpty();
  }

  @Ignore
  @Test
  public void test() {
    String tagmlIn = "[text}[p=p-1}This is a simple paragraph.{p=p-1]{text]";
    UUID documentUUID = client.addDocumentFromTAGML(tagmlIn);
    assertThat(documentUUID).isNotNull();

    String tagmlOut = client.getTAGML(documentUUID);
    assertThat(tagmlOut).isNotNull();
    // assertThat(tagmlOut).isEqualTo(tagmlIn);

    String latex = client.getDocumentLaTeX(documentUUID);
    assertThat(latex).isNotEmpty();

    String latex2 = client.getKdTreeLaTex(documentUUID);
    assertThat(latex2).isNotEmpty();

    String latex3 = client.getMarkupDepthLaTex(documentUUID);
    assertThat(latex3).isNotEmpty();

    String latex4 = client.getMatrixLaTex(documentUUID);
    assertThat(latex4).isNotEmpty();

    JsonNode queryResult = client.postTAGQLQuery(documentUUID, "select text from markup('text')");
    assertThat(queryResult).isNotNull();
    JsonNode values = queryResult.get("values");
    assertThat(values).isNotNull();
    JsonNode value = values.get(0);
    assertThat(value).isNotNull();
    assertThat(value.asText()).isEqualTo("This is a simple paragraph.");

  }

  /// end tests

  boolean returnsRestResult(Method method) {
    return method.getReturnType().equals(RestResult.class);
  }

  boolean hasNoDelegatedMethodInOptimisticAlexandriaMarkupClient(Method method) {
    Class<OptimisticAlexandriaMarkupClient> o = OptimisticAlexandriaMarkupClient.class;
    try {
      Method oMethod = o.getMethod(method.getName(), method.getParameterTypes());
      Type type = actualReturnType(method);
      boolean equals = type.equals(Void.class)//
          ? oMethod.getReturnType().equals(Void.TYPE)//
          : oMethod.getReturnType().equals(type);
      return !equals;
    } catch (Exception e) {
      return true;
    }
  }

  private Type actualReturnType(Method method) {
    Type genericReturnType = method.getGenericReturnType();
    return ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
  }

  String toDelegatedMethodStub(Method method) {
    String returnType = actualReturnType(method).getTypeName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "").replace("Void", "void");
    String methodName = method.getName();
    String qualifiedParameters = Arrays.stream(method.getParameters())//
        .map(this::toQualifiedParameter)//
        .collect(joining(", "));
    String returnStatement = "void".equals(returnType) ? "" : "return ";
    String parameters = Arrays.stream(method.getParameters())//
        .map(this::parameterName)//
        .collect(joining(", "));

    return MessageFormat.format(//
        "public {0} {1}({2}) '{' {3}unwrap(delegate.{4}({5}));'}'", //
        returnType, //
        methodName, //
        qualifiedParameters, //
        returnStatement, //
        methodName, //
        parameters//
    );
  }

  String toQualifiedParameter(Parameter parameter) {
    return typeString(parameter) + " " + parameterName(parameter);
  }

  private String typeString(Parameter parameter) {
    return parameter.getType().getName().replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX, "");
  }

  String parameterName(Parameter parameter) {
    return typeString(parameter).toLowerCase();
  }

}
