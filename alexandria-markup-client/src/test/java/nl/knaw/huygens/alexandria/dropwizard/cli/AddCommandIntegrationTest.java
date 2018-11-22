package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AddCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testAddCommand() throws Exception {
    final boolean success = cli.run("add", "transcription1.tagml", "transcription2.tagml");
    assertSucceedsWithExpectedStdout(success, "");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getWatchedFiles()).containsExactlyInAnyOrder("transcription1.tagml", "transcription2.tagml");
  }

  @Test
  public void testAddCommandWithoutParametersFails() throws Exception {
    final boolean success = cli.run("add");
    softlyAssertFailsWithExpectedStderr(success, "too few arguments\n" +
        "usage: java -jar alexandria-app.jar\n" +
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

}
