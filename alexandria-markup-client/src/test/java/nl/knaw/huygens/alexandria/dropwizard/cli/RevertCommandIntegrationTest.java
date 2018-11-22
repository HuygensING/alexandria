package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class RevertCommandIntegrationTest extends CommandIntegrationTest {
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

}
