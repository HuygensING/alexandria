package nl.knaw.huygens.alexandria.dropwizard.cli;

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
import java.util.Optional;

import static nl.knaw.huygens.alexandria.markup.api.AlexandriaProperties.WORKDIR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CommandIntegrationTest {
  Logger LOG = LoggerFactory.getLogger(this.getClass());

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
  Cli cli;
  private Path workDirectory;

  @Before
  public void setUp() throws Exception {
    setupWorkDir();

    // Setup necessary mock
    final JarLocation location = mock(JarLocation.class);
    when(location.getVersion()).thenReturn(Optional.of("1.0.0"));
    when(location.toString()).thenReturn("alexandria-app.jar");

    // Add commands you want to test
    final Bootstrap<ServerConfiguration> bootstrap = new Bootstrap<>(new ServerApplication());
    bootstrap.addCommand(new AddCommand());
    bootstrap.addCommand(new CheckInCommand());
    bootstrap.addCommand(new CheckOutCommand());
    bootstrap.addCommand(new CommitCommand());
    bootstrap.addCommand(new DiffCommand());
    bootstrap.addCommand(new HelpCommand());
    bootstrap.addCommand(new InitCommand());
    bootstrap.addCommand(new RevertCommand());
    final AppInfo appInfo = new AppInfo().setVersion("$version$").setBuildDate("$buildDate$");
    bootstrap.addCommand(new StatusCommand().withAppInfo(appInfo));

    // Redirect stdout and stderr to our byte streams
    System.setOut(new PrintStream(stdOut));
    System.setErr(new PrintStream(stdErr));

    // Build what'll run the command and interpret arguments
    cli = new Cli(location, bootstrap, stdOut, stdErr);
  }

  private void setupWorkDir() throws IOException {
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

  private void softlyAssertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isTrue();
    softly.assertThat(stdOut.toString().trim()).as("stdout").isEqualTo(expectedOutput.trim());
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
    System.out.println(json);
    CLIContext cliContext = new ObjectMapper().readValue(json, CLIContext.class);
    return cliContext;
  }

}