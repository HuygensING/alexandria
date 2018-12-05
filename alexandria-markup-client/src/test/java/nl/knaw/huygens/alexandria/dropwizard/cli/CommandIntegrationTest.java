package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.cli.Cli;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.JarLocation;
import nl.knaw.huygens.alexandria.dropwizard.ServerApplication;
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.AddCommand;
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CheckOutCommand;
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.CommitCommand;
import nl.knaw.huygens.alexandria.dropwizard.cli.commands.InitCommand;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

import static java.lang.String.format;
import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand.SOURCE_DIR;
import static nl.knaw.huygens.alexandria.dropwizard.cli.commands.AlexandriaCommand.VIEWS_DIR;
import static nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties.WORKDIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CommandIntegrationTest {
  Logger LOG = LoggerFactory.getLogger(this.getClass());

  Cli cli;

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
//  private final InputStream originalIn = System.in;

  final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
  final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
  private Path workDirectory;
  static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.findAndRegisterModules();
  }

  private static final String INIT = new InitCommand().getName();
  private static final String COMMIT = new CommitCommand().getName();
  private static final String ADD = new AddCommand().getName();
  private static final String CHECKOUT = new CheckOutCommand().getName();

  @Before
  public void setUp() throws IOException {
    setupWorkDir();

    // Setup necessary mock
    final JarLocation location = mock(JarLocation.class);
    when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
    when(location.toString()).thenReturn("alexandria-app.jar");

    // Add commands you want to test
    ServerApplication serverApplication = new ServerApplication();
    final Bootstrap<ServerConfiguration> bootstrap = new Bootstrap<>(serverApplication);
    final AppInfo appInfo = new AppInfo()
        .setVersion("$version$")
        .setBuildDate("$buildDate$");
    serverApplication.addCommands(bootstrap, appInfo);

    // Redirect stdout and stderr to our byte streams
    System.setOut(new PrintStream(stdOut));
    System.setErr(new PrintStream(stdErr));

    // Build what'll run the command and interpret arguments
    cli = new Cli(location, bootstrap, stdOut, stdErr);
  }

  private void setupWorkDir() throws IOException {
    workDirectory = Files.createTempDirectory("alexandria");
//    String tmpDir = System.getProperty("java.io.tmpdir");
//    workDirectory = Paths.get(tmpDir, "alexandria_testdir");
    FileUtils.deleteQuietly(workDirectory.toFile());
    workDirectory.toFile().mkdir();
    System.setProperty(WORKDIR, workDirectory.toAbsolutePath().toString());
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    System.setErr(originalErr);
//    System.setIn(originalIn);
    tearDownWorkDir();
  }

  private void tearDownWorkDir() throws IOException {
    File directory = new File(workDirectory.toAbsolutePath().toString());
    FileUtils.forceDeleteOnExit(directory);
  }

  void softlyAssertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isTrue();

    String normalizedExpectedOutput = normalize(expectedOutput);
    String normalizedStdOut = normalize(stdOut.toString());
    softly.assertThat(normalizedStdOut).as("stdout").isEqualTo(normalizedExpectedOutput);
    softly.assertThat(stdErr.toString().trim()).as("stderr").isEmpty();
    softly.assertAll();
    resetStdOutErr();
  }

  void assertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    assertThat(success).as("Exit success").isTrue();

    String normalizedExpectedOutput = normalize(expectedOutput);
    String normalizedStdOut = normalize(stdOut.toString());
    assertThat(normalizedStdOut).as("stdout").isEqualTo(normalizedExpectedOutput);

    String normalizedErrors = stdErr.toString().trim();
    assertThat(normalizedErrors).as("stderr").isEmpty();
    resetStdOutErr();
  }

  void softlyAssertFailsWithExpectedStderr(final boolean success, final String expectedError) {
    String normalizedExpectedError = normalize(expectedError);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isFalse();
    String normalizeStdErr = normalize(stdErr.toString());
    softly.assertThat(normalizeStdErr).as("stderr").isEqualTo(normalizedExpectedError);
    softly.assertAll();
    resetStdOutErr();
  }

  void assertFailsWithExpectedStderr(final boolean success, final String expectedError) {
    String normalizedExpectedError = normalize(expectedError);
    assertThat(success).as("Exit success").isFalse();
    String normalizeStdErr = normalize(stdErr.toString());
    assertThat(normalizeStdErr).as("stderr").isEqualTo(normalizedExpectedError);
    resetStdOutErr();
  }

  private void resetStdOutErr() {
    stdOut.reset();
    stdErr.reset();
  }

  private String normalize(final String string) {
    return string.trim()
        .replace(System.lineSeparator(), "\n");
  }

  Path workFilePath(final String relativePath) {
    return workDirectory.resolve(relativePath);
  }

  CLIContext readCLIContext() throws IOException {
    Path contextPath = workFilePath(".alexandria/context.json");
    assertThat(contextPath).isRegularFile();
    String json = new String(Files.readAllBytes(contextPath));
    assertThat(json).isNotEmpty();
//    System.out.println(json);
    return mapper.readValue(json, CLIContext.class);
  }

  String getCliStdOutAsString() {
    return stdOut.toString();
  }

  String getCliStdErrAsString() {
    return stdErr.toString();
  }

  void runInitCommand() throws Exception {
    assertThat(cli.run(INIT)).isTrue();
    resetStdOutErr();
  }

  void runAddCommand(String... fileNames) throws Exception {
    List<String> arguments = new ArrayList<>();
    arguments.add(ADD);
    Collections.addAll(arguments, fileNames);
    String[] argumentArray = arguments.toArray(new String[]{});
    assertThat(cli.run(argumentArray)).isTrue();
    resetStdOutErr();
  }

  void runCommitAllCommand() throws Exception {
    final boolean success = cli.run(COMMIT, "-a");
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).isTrue();
    softly.assertThat(getCliStdErrAsString()).isEmpty();
    softly.assertAll();
    resetStdOutErr();
  }

  void runCheckoutCommand(final String viewName) throws Exception {
    final boolean success = cli.run(CHECKOUT, viewName);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).isTrue();
    softly.assertThat(getCliStdErrAsString()).isEmpty();
    softly.assertAll();
    resetStdOutErr();
  }

  void assertCommandRunsInAnInitializedDirectory(final String... cliArguments) throws Exception {
    final boolean success = cli.run(cliArguments);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).isFalse();
    softly.assertThat(getCliStdOutAsString()).contains("This directory has not been initialized");
    softly.assertThat(getCliStdErrAsString().trim()).isEqualTo("not initialized");
    softly.assertAll();
  }

  void createFile(String filename, String content) throws IOException {
    Path file = workFilePath(filename);
    Path result = Files.createFile(file);
    assertThat(result).isNotNull();
    assertThat(result).hasContent("");
    if (!content.isEmpty()) {
      Path writeResult = Files.write(file, content.getBytes());
      assertThat(writeResult).isNotNull();
      assertThat(writeResult).hasContent(content);
    }
  }

  void modifyFile(String filename, String content) throws IOException {
    Path file = workFilePath(filename);
    if (!content.isEmpty()) {
      Files.write(file, content.getBytes());
    }
  }

  void deleteFile(String filename) throws IOException {
    Path file = workFilePath(filename);
    Files.delete(file);
  }

  String readFileContents(String filename) throws IOException {
    Path file1 = workFilePath(filename);
    return new String(Files.readAllBytes(file1));
  }

  protected Instant readLastCommittedInstant(String filename) throws IOException {
    CLIContext cliContext = readCLIContext();
    Map<String, FileInfo> watchedFiles = cliContext.getWatchedFiles();
    return watchedFiles.get(filename).getLastCommit();
  }

  String createViewFileName(final String viewName) {
    return format("%s/%s.json", VIEWS_DIR, viewName);
  }

  String createTagmlFileName(final String documentName) {
    return format("%s/%s.tagml", SOURCE_DIR, documentName);
  }

}
