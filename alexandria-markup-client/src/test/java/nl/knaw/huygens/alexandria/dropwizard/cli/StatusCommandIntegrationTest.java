package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class StatusCommandIntegrationTest extends CommandIntegrationTest {
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
}
