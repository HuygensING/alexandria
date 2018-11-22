package nl.knaw.huygens.alexandria.dropwizard.cli;

import org.junit.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class InitCommandIntegrationTest extends CommandIntegrationTest {
  @Test
  public void testInitCommand() throws Exception {
    final boolean success = cli.run("init");
    assertSucceedsWithExpectedStdout(success, "initializing...\n" +
        "done!");

    Path viewsJson = workFilePath(".alexandria/views.json");
    assertThat(viewsJson).hasContent("{}");

    CLIContext cliContext = readCLIContext();
    assertThat(cliContext.getActiveView()).isEqualTo("-");
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

}
