package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class CheckOutCommandIntegrationTest extends CommandIntegrationTest {
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

}
