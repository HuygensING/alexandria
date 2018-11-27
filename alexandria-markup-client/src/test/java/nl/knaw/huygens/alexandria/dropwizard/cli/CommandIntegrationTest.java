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
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties.WORKDIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CommandIntegrationTest {
  Logger LOG = LoggerFactory.getLogger(this.getClass());

  Cli cli;

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
  private Path workDirectory;
  static ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.findAndRegisterModules();
  }

  @Before
  public void setUp() {
    setupWorkDir();

    // Setup necessary mock
    final JarLocation location = mock(JarLocation.class);
    when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
    when(location.toString()).thenReturn("alexandria-app.jar");

    // Add commands you want to test
    final Bootstrap<ServerConfiguration> bootstrap = new Bootstrap<>(new ServerApplication());
    bootstrap.addCommand(new HelpCommand());
    bootstrap.addCommand(new InitCommand());
    bootstrap.addCommand(new AddCommand());
    bootstrap.addCommand(new CommitCommand());
    bootstrap.addCommand(new CheckOutCommand());
    bootstrap.addCommand(new DiffCommand());
    bootstrap.addCommand(new RevertCommand());
    final AppInfo appInfo = new AppInfo().setVersion("$version$").setBuildDate("$buildDate$");
    bootstrap.addCommand(new StatusCommand().withAppInfo(appInfo));

    // Redirect stdout and stderr to our byte streams
    System.setOut(new PrintStream(stdOut));
    System.setErr(new PrintStream(stdErr));

    // Build what'll run the command and interpret arguments
    cli = new Cli(location, bootstrap, stdOut, stdErr);
  }

  private void setupWorkDir() {
    String tmpDir = System.getProperty("java.io.tmpdir");
    workDirectory = Paths.get(tmpDir, "alexandria_testdir");
    FileUtils.deleteQuietly(workDirectory.toFile());
    workDirectory.toFile().mkdir();
    System.setProperty(WORKDIR, workDirectory.toAbsolutePath().toString());
  }

  @After
  public void tearDown() throws IOException {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
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
    softly.assertThat(stdOut.toString().trim()).as("stdout").isEqualTo(normalizedExpectedOutput);
    softly.assertThat(stdErr.toString().trim()).as("stderr").isEmpty();
    softly.assertAll();
  }

  void assertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    assertThat(success).as("Exit success").isTrue();

    String normalizedExpectedOutput = normalize(expectedOutput);
    String normalizedOutput = stdOut.toString().trim();
    assertThat(normalizedOutput).as("stdout").isEqualTo(normalizedExpectedOutput);

    String normalizedErrors = stdErr.toString().trim();
    assertThat(normalizedErrors).as("stderr").isEmpty();
  }

  void softlyAssertFailsWithExpectedStderr(final boolean success, final String expectedError) {
    String normalizedExpectedError = normalize(expectedError);
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isFalse();
    softly.assertThat(stdErr.toString().trim()).as("stderr").isEqualTo(normalizedExpectedError);
    softly.assertAll();
  }

  private void assertFailsWithExpectedStderr(final boolean success, final String expectedError) {
    String normalizedExpectedError = normalize(expectedError);
    assertThat(success).as("Exit success").isFalse();
    assertThat(stdErr.toString().trim()).as("stderr").isEqualTo(normalizedExpectedError);
  }

  private String normalize(final String expectedOutput) {
    return expectedOutput.trim()
        .replace("\n", System.lineSeparator());
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
    assertThat(cli.run("init")).isTrue();
    stdOut.reset();
    stdErr.reset();
  }

  void runAddCommand(String... filenames) throws Exception {
    List<String> arguments = new ArrayList<>();
    arguments.add("add");
    Collections.addAll(arguments, filenames);
    String[] argumentArray = arguments.toArray(new String[]{});
    assertThat(cli.run(argumentArray)).isTrue();
    stdOut.reset();
    stdErr.reset();
  }

  void assertCommandRunsInAnInitializedDirectory(final String... cliArguments) throws Exception {
    final boolean success = cli.run(cliArguments);
    assertThat(success).isFalse();
    assertThat(getCliStdErrAsString()).isEqualTo("Initialize first");
  }

  void createFile(String filename, String content) throws IOException {
    Path file1 = workFilePath(filename);
    Path file = Files.createFile(file1);
    if (!content.isEmpty()) {
      Files.write(file, content.getBytes());
    }
  }

  protected Instant readLastCommittedInstant(String filename) throws IOException {
    CLIContext cliContext = readCLIContext();
    Map<String, Instant> watchedFiles = cliContext.getWatchedFiles();
    return watchedFiles.get(filename);
  }
}
