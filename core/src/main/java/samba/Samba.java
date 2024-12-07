package samba;

import samba.config.InvalidConfigurationException;
import samba.config.SambaConfiguration;
import samba.node.Node;
import samba.samba.SambaDefaultExceptionHandler;
import samba.samba.exceptions.ExceptionUtil;
import samba.services.storage.DatabaseStorageException;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;

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
    try {

      final Node node = new PortalNode(SambaConfiguration.builder().build());
      nodeRef.set(node);
      node.start();

    } catch (final Throwable t) {
      handleExceptionAndReturnExitCode(t);
    }
    return Optional.ofNullable(nodeRef.get());
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

  public static void handleExceptionAndReturnExitCode(final Throwable e) {
    final Optional<Throwable> maybeUserErrorException =
        ExceptionUtil.<Throwable>getCause(e, InvalidConfigurationException.class)
            .or(() -> ExceptionUtil.getCause(e, DatabaseStorageException.class));
    if (maybeUserErrorException.isPresent()) {
      LogManager.getLogger().fatal(e.getMessage(), e);
    } else {
      LogManager.getLogger().fatal("Samba failed to start", e);
    }
  }
}
