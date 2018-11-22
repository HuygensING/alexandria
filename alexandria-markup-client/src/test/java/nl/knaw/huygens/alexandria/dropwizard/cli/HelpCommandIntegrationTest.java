package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

public class HelpCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testHelpCommand() throws Exception {
    final boolean success = cli.run("help");
    assertSucceedsWithExpectedStdout(success, "TODO");
  }

}
