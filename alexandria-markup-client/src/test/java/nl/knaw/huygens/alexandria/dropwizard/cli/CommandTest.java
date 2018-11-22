package nl.knaw.huygens.alexandria.dropwizard.cli;

import io.dropwizard.cli.Cli;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.JarLocation;
import nl.knaw.huygens.alexandria.dropwizard.ServerApplication;
import nl.knaw.huygens.alexandria.dropwizard.ServerConfiguration;
import nl.knaw.huygens.alexandria.markup.api.AppInfo;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CommandTest {

  private final PrintStream originalOut = System.out;
  private final PrintStream originalErr = System.err;
  private final InputStream originalIn = System.in;

  private final ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
  private final ByteArrayOutputStream stdErr = new ByteArrayOutputStream();
  private Cli cli;

  @Before
  public void setUp() throws Exception {
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

  @After
  public void teardown() {
    System.setOut(originalOut);
    System.setErr(originalErr);
    System.setIn(originalIn);
  }

  @Test
  public void testAddCommand() throws Exception {
    final boolean success = cli.run("add");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testAddCommandHelp() throws Exception {
    final boolean success = cli.run("add", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       add [-h] FILE [FILE ...]\n" +
        "\n" +
        "Add file context to the index\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the files to watch\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCheckInCommand() throws Exception {
    final boolean success = cli.run("checkin");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testCheckInCommandHelp() throws Exception {
    final boolean success = cli.run("checkin", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       checkin [-h] file\n" +
        "\n" +
        "Merge the changes in the view into the TAG\n" +
        "\n" +
        "positional arguments:\n" +
        "  file                   The file containing the edited view\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCheckOutCommand() throws Exception {
    final boolean success = cli.run("checkout");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testCheckOutCommandHelp() throws Exception {
    final boolean success = cli.run("checkout", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       checkout [-h] VIEW\n" +
        "\n" +
        "Activate or deactivate a view in this directory\n" +
        "\n" +
        "positional arguments:\n" +
        "  VIEW                   The name of the view to use\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testCommitCommand() throws Exception {
    final boolean success = cli.run("commit");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testCommitCommandHelp() throws Exception {
    final boolean success = cli.run("commit", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       commit [-a A] [-h] FILE [FILE ...]\n" +
        "\n" +
        "Record changes to the repository\n" +
        "\n" +
        "positional arguments:\n" +
        "  FILE                   the changed file(s)\n" +
        "\n" +
        "named arguments:\n" +
        "  -a A                   automatically add all changed files\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testDiffCommand() throws Exception {
    final boolean success = cli.run("diff");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testDiffCommandHelp() throws Exception {
    final boolean success = cli.run("diff", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       diff [-h] file\n" +
        "\n" +
        "Show the changes made to the view.\n" +
        "\n" +
        "positional arguments:\n" +
        "  file                   The file containing the edited view\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testHelpCommand() throws Exception {
    final boolean success = cli.run("help");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testInitCommand() throws Exception {
    final boolean success = cli.run("init");
    assertSucceedsWithExpectedStdout(success, "initializing...\n" +
        "done!");
  }

  @Test
  public void testInitCommandHelp() throws Exception {
    final boolean success = cli.run("init", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       init [-h]\n" +
        "\n" +
        "Initializes current directory as an alexandria workspace\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testRevertCommand() throws Exception {
    final boolean success = cli.run("revert");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testRevertCommandHelp() throws Exception {
    final boolean success = cli.run("revert", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       revert [-h] file\n" +
        "\n" +
        "Revert the changes in the view.\n" +
        "\n" +
        "positional arguments:\n" +
        "  file                   The file to be reset\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  @Test
  public void testStatusCommand() throws Exception {
    final boolean success = cli.run("status");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

  @Test
  public void testStatusCommandHelp() throws Exception {
    final boolean success = cli.run("status", "-h");
    assertSucceedsWithExpectedStdout(success, "usage: java -jar alexandria-app.jar\n" +
        "       status [-h]\n" +
        "\n" +
        "Show info about the alexandria graph and the directory status.\n" +
        "\n" +
        "named arguments:\n" +
        "  -h, --help             show this help message and exit");
  }

  private void softlyAssertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(success).as("Exit success").isTrue();
    softly.assertThat(stdOut.toString().trim()).as("stdout").isEqualTo(expectedOutput.trim());
    softly.assertThat(stdErr.toString().trim()).as("stderr").isEmpty();
    softly.assertAll();
  }

  private void assertSucceedsWithExpectedStdout(final boolean success, final String expectedOutput) {
    assertThat(success).as("Exit success").isTrue();

    String normalizedExpectedOutput = expectedOutput.trim()
        .replace("\n", System.lineSeparator());
    String normalizedOutput = stdOut.toString().trim();
    assertThat(normalizedOutput).as("stdout").isEqualTo(normalizedExpectedOutput);

    String normalizedErrors = stdErr.toString().trim();
    assertThat(normalizedErrors).as("stderr").isEmpty();
  }
}