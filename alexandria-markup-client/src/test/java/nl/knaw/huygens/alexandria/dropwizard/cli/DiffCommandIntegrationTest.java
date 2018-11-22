package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class DiffCommandIntegrationTest extends CommandIntegrationTest {
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

}
