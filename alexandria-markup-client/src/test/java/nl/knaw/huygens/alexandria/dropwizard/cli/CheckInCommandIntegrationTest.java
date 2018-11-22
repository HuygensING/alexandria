package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class CheckInCommandIntegrationTest extends CommandIntegrationTest {
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

}
