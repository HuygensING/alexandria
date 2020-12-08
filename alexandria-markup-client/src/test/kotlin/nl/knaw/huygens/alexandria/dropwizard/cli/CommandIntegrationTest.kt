package nl.knaw.huygens.alexandria.dropwizard.cli
/*-
 * #%L
 * alexandria-markup-client
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

import com.fasterxml.jackson.databind.ObjectMapper
import io.dropwizard.cli.Cli
import io.dropwizard.setup.Bootstrap
import io.dropwizard.util.JarLocation
import nl.knaw.huygens.alexandria.dropwizard.ServerApplication
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.*
import nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties.WORKDIR
import nl.knaw.huygens.alexandria.markup.api.AppInfo
import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.After
import org.junit.Before
import org.mockito.Mockito
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*

abstract class CommandIntegrationTest {
    var logger: Logger = LoggerFactory.getLogger(this.javaClass)
    var cli: Cli? = null
    private var originalOut: PrintStream? = null
    private var originalErr: PrintStream? = null
    var stdOut: ByteArrayOutputStream? = null
    var stdErr: ByteArrayOutputStream? = null

    @Before
    @Throws(IOException::class)
    fun setUp() {
        setupWorkDir()

        // Setup necessary mock
        location = Mockito.mock(JarLocation::class.java)
        Mockito.`when`(location!!.version).thenReturn(Optional.of("1.0.0"))
        Mockito.`when`(location!!.toString()).thenReturn("alexandria-app.jar")

        // Add commands you want to test
        val serverApplication = ServerApplication()
        bootstrap = Bootstrap(serverApplication)
        val appInfo: AppInfo = AppInfo().setVersion("\$version$").setBuildDate("\$buildDate$")
        serverApplication.addCommands(bootstrap, appInfo)

        resetCli()
    }

    internal fun resetCli() {
        // Redirect stdout and stderr to our byte streams
        originalOut = System.out
        stdOut = ByteArrayOutputStream()
        System.setOut(PrintStream(stdOut!!))
        originalErr = System.err
        stdErr = ByteArrayOutputStream()
        System.setErr(PrintStream(stdErr!!))

        // Build what'll run the command and interpret arguments
        cli = Cli(location, bootstrap, stdOut, stdErr)
    }

    @Throws(IOException::class)
    private fun setupWorkDir() {
        workDirectory = Files.createTempDirectory("alexandria")
        FileUtils.deleteQuietly(workDirectory!!.toFile())
        workDirectory!!.toFile().mkdir()
        System.setProperty(WORKDIR, workDirectory!!.toAbsolutePath().toString())
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        stdOut = null
        System.setOut(originalOut)
        stdErr = null
        System.setErr(originalErr)
        tearDownWorkDir()
    }

    @Throws(IOException::class)
    private fun tearDownWorkDir() {
        val directory = File(workDirectory!!.toAbsolutePath().toString())
        FileUtils.forceDeleteOnExit(directory)
    }

    fun softlyAssertSucceedsWithStdoutContaining(
        success: Boolean?, outputSubString: String?
    ) {
        SoftAssertions().apply {
            assertThat(success).`as`("Exit success").isTrue

            //    String normalizedExpectedOutput = normalize(expectedOutput);
            val normalizedStdOut = stdOut.toString().normalized()
            assertThat(normalizedStdOut).`as`("stdout").contains(outputSubString)
            assertThat(stdErr.toString().trim { it <= ' ' }).`as`("stderr").isEmpty()
            assertAll()
        }
        resetStdOutErr()
    }

    fun softlyAssertSucceedsWithExpectedStdout(success: Boolean?, expectedOutput: String) {
        SoftAssertions().apply {
            assertThat(success).`as`("Exit success").isTrue

            val normalizedExpectedOutput = expectedOutput.normalized()
            val normalizedStdOut = stdOut.toString().normalized()
            assertThat(normalizedStdOut).`as`("stdout").isEqualTo(normalizedExpectedOutput)
            assertThat(stdErr.toString().trim { it <= ' ' }).`as`("stderr").isEmpty()
            assertAll()
        }
        resetStdOutErr()
    }

    fun assertSucceedsWithExpectedStdout(success: Boolean?, expectedOutput: String) {
        SoftAssertions().apply {
            assertThat(success).`as`("Exit success").isTrue

            val normalizedExpectedOutput = expectedOutput.normalized()
            val normalizedStdOut = stdOut.toString().normalized()
            assertThat(normalizedStdOut).`as`("stdout").isEqualTo(normalizedExpectedOutput)

            val normalizedErrors = stdErr.toString().trim { it <= ' ' }
            assertThat(normalizedErrors).`as`("stderr").isEmpty()
            assertAll()
        }
        resetStdOutErr()
    }

    fun softlyAssertFailsWithExpectedStderr(success: Boolean?, expectedError: String) {
        val normalizedExpectedError = expectedError.normalized()
        SoftAssertions().apply {
            assertThat(success).`as`("Exit success").isFalse
            val normalizeStdErr = stdErr.toString().normalized()
            assertThat(normalizeStdErr).`as`("stderr").isEqualTo(normalizedExpectedError)

            //    String normalizedStdOut = normalize(stdOut.toString());
            //    assertThat(normalizedStdOut).as("stdout").isEqualTo("");
            assertAll()
        }
        resetStdOutErr()
    }

    fun assertFailsWithExpectedStderr(success: Boolean?, expectedError: String) {
        SoftAssertions().apply {
            val normalizedExpectedError = expectedError.normalized()
            assertThat(success).`as`("Exit success").isFalse

            val normalizeStdErr = stdErr.toString().normalized()
            assertThat(normalizeStdErr).`as`("stderr").isEqualTo(normalizedExpectedError)
            assertAll()
        }
        resetStdOutErr()
    }

    fun assertFailsWithExpectedStdoutAndStderr(
        success: Boolean,
        expectedOutput: String,
        expectedError: String
    ) {
        assertThat(success).`as`("Exit success").isFalse
        val normalizedExpectedOutput = expectedOutput.normalized()
        val normalizedStdOut = stdOut.toString().normalized()
        assertThat(normalizedStdOut).`as`("stdout").isEqualTo(normalizedExpectedOutput)
        val normalizedExpectedError = expectedError.normalized()
        val normalizeStdErr = stdErr.toString().normalized()
        assertThat(normalizeStdErr).`as`("stderr").isEqualTo(normalizedExpectedError)
        resetStdOutErr()
    }

    private fun resetStdOutErr() {
        stdOut!!.reset()
        stdErr!!.reset()
        assertThat(stdOut.toString()).isEmpty()
        assertThat(stdErr.toString()).isEmpty()
    }

    @Throws(IOException::class)
    fun readCLIContext(): CLIContext {
        val contextPath = workFilePath(".alexandria/context.json")
        assertThat(contextPath).isRegularFile
        val json = String(Files.readAllBytes(contextPath))
        assertThat(json).isNotEmpty
        return mapper.readValue(json, CLIContext::class.java)
    }

    val cliStdOutAsString: String
        get() = stdOut.toString()

    val cliStdErrAsString: String
        get() = stdErr.toString()

    @Throws(Exception::class)
    fun runInitCommand() {
        assertThat(cli!!.run(INIT)).overridingErrorMessage(stdErr.toString()).isTrue
        resetStdOutErr()
    }

    @Throws(Exception::class)
    fun runAddCommand(vararg fileNames: String) {
        val arguments: MutableList<String> = ArrayList()
        arguments.add(ADD)
        Collections.addAll(arguments, *fileNames)
        val argumentArray: Array<String> = arguments.toTypedArray()
        //    System.out.println("#" + Paths.get("").toAbsolutePath().toString());
        assertThat(cli!!.run(*argumentArray)).overridingErrorMessage(stdErr.toString()).isTrue
        resetStdOutErr()
    }

    @Throws(Exception::class)
    fun runCommitAllCommand() {
        val throwable = cli!!.run(COMMIT, "-a")
        logger.info("stdOut={}", stdOut.toString())
        SoftAssertions().apply {
            assertThat(throwable).overridingErrorMessage(stdErr.toString()).isTrue
            assertThat(cliStdErrAsString).isEmpty()
            assertAll()
        }
        resetStdOutErr()
    }

    @Throws(Exception::class)
    fun runCheckoutCommand(viewName: String) {
        val throwable = cli!!.run(CHECKOUT, viewName)
        SoftAssertions().apply {
            assertThat(throwable).overridingErrorMessage(stdErr.toString()).isTrue
            assertThat(cliStdErrAsString).isEmpty()
            assertThat(readCLIContext().activeView).isEqualTo(viewName)
            assertAll()
        }
        resetStdOutErr()
    }

    @Throws(Exception::class)
    fun assertCommandRunsInAnInitializedDirectory(vararg cliArguments: String) {
        val throwable = cli!!.run(*cliArguments)
        SoftAssertions().apply {
            assertThat(throwable).isFalse
            assertThat(cliStdOutAsString)
                .contains("This directory (or any of its parents) has not been initialized")
            assertThat(cliStdErrAsString.trim { it <= ' ' }).isEqualTo("not initialized")
            assertAll()
        }
    }

    @Throws(IOException::class)
    fun createFile(filename: String, content: String): String {
        val file = workFilePath(filename)
        val result = Files.createFile(file)
        assertThat(result).isNotNull.hasContent("")
        if (content.isNotEmpty()) {
            val writeResult = Files.write(file, content.toByteArray())
            assertThat(writeResult).isNotNull.hasContent(content)
        }
        return result.toAbsolutePath().toString()
    }

    @Throws(IOException::class)
    fun createDirectory(directoryName: String): String {
        val directory = workFilePath(directoryName)
        val result = Files.createDirectory(directory)
        assertThat(result).isNotNull.isDirectory
        return result.toAbsolutePath().toString()
    }

    @Throws(IOException::class)
    fun modifyFile(filename: String, content: String) {
        val file = workFilePath(filename)
        if (content.isNotEmpty()) {
            Files.write(file, content.toByteArray())
        }
    }

    @Throws(IOException::class)
    fun deleteFile(filename: String) {
        val file = workFilePath(filename)
        Files.delete(file)
    }

    @Throws(IOException::class)
    fun readFileContents(filename: String): String {
        val file1 = workFilePath(filename)
        return String(Files.readAllBytes(file1))
    }

    @Throws(IOException::class)
    protected fun readLastCommittedInstant(filename: String): Instant {
        val cliContext = readCLIContext()
        val watchedFiles = cliContext.watchedFiles
        return watchedFiles[filename]!!.lastCommit
    }

    companion object {
        var workDirectory: Path? = null
        var mapper = ObjectMapper()

        private var location: JarLocation? = null
        private var bootstrap: Bootstrap<ServerConfiguration>? = null
        private val INIT = InitCommand().name
        private val COMMIT = CommitCommand().name
        private val ADD = AddCommand().name
        private val CHECKOUT = CheckOutCommand().name

        init {
            mapper.findAndRegisterModules()
        }

        fun String.normalized(): String = trim { it <= ' ' }
            .replace(System.lineSeparator(), "\n")

        fun workFilePath(relativePath: String): Path = workDirectory!!.resolve(relativePath)

        fun createViewFileName(viewName: String): String =
            String.format("%s/%s.json", AlexandriaCommand.VIEWS_DIR, viewName)

        fun createTagmlFileName(documentName: String): String =
            String.format("%s/%s.tagml", AlexandriaCommand.SOURCE_DIR, documentName)

    }

}
