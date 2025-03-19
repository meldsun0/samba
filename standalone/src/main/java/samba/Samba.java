package samba;

import samba.cli.SambaCommand;
import samba.config.SambaConfiguration;
import samba.node.Node;
import samba.samba.SambaDefaultExceptionHandler;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class Samba {

  public static void main(String[] args) {
    System.out.println("Received arguments: " + Arrays.toString(args));
    Thread.setDefaultUncaughtExceptionHandler(new SambaDefaultExceptionHandler());
    try {
      Optional<Node> maybeNode = Samba.startFromCLIArgs(args);
      maybeNode.ifPresent(
          node ->
              Runtime.getRuntime()
                  .addShutdownHook(
                      new Thread(
                          () -> {
                            System.out.println("Samba is shutting down");
                            node.stop();
                          })));
    } catch (CLIException e) {
      System.exit(e.getResultCode());
    }
  }

  static Optional<Node> startFromCLIArgs(final String[] cliArgs) throws CLIException {
    AtomicReference<Node> nodeRef = new AtomicReference<>();
    int result = start((config) -> nodeRef.set(start(config)), cliArgs);
    if (result != 0) {
      throw new CLIException(result);
    }
    return Optional.ofNullable(nodeRef.get());
  }

  private static int start(final StartAction startAction, final String... args) {
    final PrintWriter outputWriter = new PrintWriter(System.out, true, Charset.defaultCharset());
    final PrintWriter errorWriter = new PrintWriter(System.err, true, Charset.defaultCharset());

    return new SambaCommand(outputWriter, errorWriter, System.getenv(), startAction).parse(args);
  }

  private static Node start(final SambaConfiguration config) {
    final Node node = new PortalNode(config);
    node.start();
    return node;
  }

  private static class CLIException extends RuntimeException {
    private final int resultCode;

    public CLIException(final int resultCode) {
      super("Unable to start Samba. Exit code: " + resultCode);
      this.resultCode = resultCode;
    }

    public int getResultCode() {
      return resultCode;
    }
  }

  @FunctionalInterface
  public interface StartAction {
    void start(SambaConfiguration config);
  }
}
