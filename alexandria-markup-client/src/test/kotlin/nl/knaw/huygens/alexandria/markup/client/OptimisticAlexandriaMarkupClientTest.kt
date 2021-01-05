package nl.knaw.huygens.alexandria.markup.client
/*
 * #%L
 * alexandria-markup-client
 * =======
 * Copyright (C) 2015 - 2021 Huygens ING (KNAW)
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

import nl.knaw.huygens.alexandria.markup.api.AppInfo
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.*
import java.lang.reflect.Method
import java.lang.reflect.Parameter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.text.MessageFormat
import java.util.*
import java.util.stream.Collectors

class OptimisticAlexandriaMarkupClientTest : AlexandriaTestWithTestMarkupServer() {

    @Before
    fun before() {
    }

    @Ignore
    @Test
    fun testAbout() {
        val about: AppInfo = client!!.about
        assertThat(about.version).isNotEmpty
    }

    @Ignore
    @Test
    fun testOptimisticAlexandriaMarkupClientHasDelegatedUnwrappedMethodForEachRelevantMethodInAlexandriaMarkupClient() {
        val a = AlexandriaMarkupClient::class.java
        val stubs = Arrays.stream(a.methods)
                .filter { method: Method -> returnsRestResult(method) }
                .filter { method: Method -> hasNoDelegatedMethodInOptimisticAlexandriaMarkupClient(method) }
                .map { method: Method -> toDelegatedMethodStub(method) }
                .collect(Collectors.joining("\n"))
        // LOG.info("Methods to add to OptimisticAlexandriaMarkupClient:\n{}", stubs);
        assertThat(stubs).isEmpty()
    }

    @Ignore
    @Test
    fun test() {
        val tagmlIn = "[text}[p=p-1}This is a simple paragraph.{p=p-1]{text]"
        val documentUUID = client!!.addDocumentFromTAGML(tagmlIn)
        assertThat(documentUUID).isNotNull
        val tagmlOut = client!!.getTAGML(documentUUID)
        assertThat(tagmlOut).isNotNull
        // assertThat(tagmlOut).isEqualTo(tagmlIn);
        val latex = client!!.getDocumentLaTeX(documentUUID)
        assertThat(latex).isNotEmpty
        val latex2 = client!!.getKdTreeLaTex(documentUUID)
        assertThat(latex2).isNotEmpty
        val latex3 = client!!.getMarkupDepthLaTex(documentUUID)
        assertThat(latex3).isNotEmpty
        val latex4 = client!!.getMatrixLaTex(documentUUID)
        assertThat(latex4).isNotEmpty
        val queryResult = client!!.postTAGQLQuery(documentUUID, "select text from markup('text')")
        assertThat(queryResult).isNotNull
        val values = queryResult["values"]
        assertThat(values).isNotNull
        val value = values[0]
        assertThat(value).isNotNull
        assertThat(value.asText()).isEqualTo("This is a simple paragraph.")
    }

    private fun hasNoDelegatedMethodInOptimisticAlexandriaMarkupClient(method: Method): Boolean {
        val o = OptimisticAlexandriaMarkupClient::class.java
        return try {
            val oMethod = o.getMethod(method.name, *method.parameterTypes)
            val type = actualReturnType(method)
            val equals = if (type == Void::class.java) oMethod.returnType == Void.TYPE else oMethod.returnType == type
            !equals
        } catch (e: Exception) {
            true
        }
    }

    private fun actualReturnType(method: Method): Type {
        val genericReturnType = method.genericReturnType
        return (genericReturnType as ParameterizedType).actualTypeArguments[0]
    }

    private fun toDelegatedMethodStub(method: Method): String {
        val returnType = actualReturnType(method)
                .typeName
                .replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX.toRegex(), "")
                .replace("Void", "void")
        val methodName = method.name
        val qualifiedParameters = Arrays.stream(method.parameters)
                .map { parameter: Parameter -> toQualifiedParameter(parameter) }
                .collect(Collectors.joining(", "))
        val returnStatement = if ("void" == returnType) "" else "return "
        val parameters = Arrays.stream(method.parameters).map { parameter: Parameter -> parameterName(parameter) }.collect(Collectors.joining(", "))
        return MessageFormat.format(
                "public {0} {1}({2}) '{' {3}unwrap(delegate.{4}({5}));'}'",
                returnType, methodName, qualifiedParameters, returnStatement, methodName, parameters)
    }

    fun toQualifiedParameter(parameter: Parameter): String =
            "${typeString(parameter)} ${parameterName(parameter)}"

    private fun typeString(parameter: Parameter): String =
            parameter
                    .type
                    .name
                    .replaceFirst(EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX.toRegex(), "")

    private fun parameterName(parameter: Parameter): String = typeString(parameter).toLowerCase()

    companion object {
        // private final Logger LOG = LoggerFactory.getLogger(getClass());
        private const val EVERYTHING_UPTO_AND_INCLUDING_THE_LAST_PERIOD_REGEX = ".*\\."
        private var client: OptimisticAlexandriaMarkupClient? = null

        @BeforeClass
        fun startClient() {
            client = OptimisticAlexandriaMarkupClient("http://localhost:2017/")
        }

        @AfterClass
        fun stopClient() {
            client!!.close()
        }

        /// end tests
        private fun returnsRestResult(method: Method): Boolean =
                method.returnType == RestResult::class.java
    }
}
