package samba;

import samba.config.SambaCommand;
import samba.config.SambaConfig;
import samba.exceptions.SambaDefaultExceptionHandler;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public final class SambaInitializer {

  public static Optional<Samba> start(String[] args) {
    try {
      System.out.println("Received arguments: " + Arrays.toString(args));
      Thread.setDefaultUncaughtExceptionHandler(new SambaDefaultExceptionHandler());
      Optional<Samba> maybeSamba = SambaInitializer.startFromCLIArgs(args);

      maybeSamba.ifPresent(
          samba ->
              Runtime.getRuntime()
                  .addShutdownHook(
                      new Thread(
                          () -> {
                            System.out.println("Samba is shutting down");
                            samba.stop();
                          })));
      return maybeSamba;
    } catch (CLIException e) {
      System.exit(e.getResultCode());
    }
    return Optional.empty();
  }

  static Optional<Samba> startFromCLIArgs(final String[] cliArgs) throws CLIException {
    AtomicReference<Samba> nodeRef = new AtomicReference<>();
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

  private static Samba start(final SambaConfig config) {
    final Samba node = new Samba(config);
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
    void start(SambaConfig config);
  }

  public static void main(String[] args) {
    SambaInitializer.start(args);
  }
}
