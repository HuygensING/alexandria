package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class CommitCommandIntegrationTest extends CommandIntegrationTest {
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

}
