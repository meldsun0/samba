package samba.exceptions;

import static samba.exceptions.ExitConstants.ERROR_EXIT_CODE;
import static samba.exceptions.ExitConstants.FATAL_EXIT_CODE;

import samba.logging.StatusLogger;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SambaDefaultExceptionHandler implements UncaughtExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SambaDefaultExceptionHandler.class);

  private final StatusLogger statusLog;

  public SambaDefaultExceptionHandler() {
    this(StatusLogger.STATUS_LOG);
  }

  @VisibleForTesting
  SambaDefaultExceptionHandler(final StatusLogger statusLog) {
    this.statusLog = statusLog;
  }

  public void handleException(
      final Throwable error,
      final Object subscriber,
      final Method invokedMethod,
      final Object[] args) {
    handleException(
        error,
        "event '"
            + invokedMethod.getDeclaringClass()
            + "."
            + invokedMethod.getName()
            + "' in handler '"
            + subscriber.getClass().getName()
            + "'");
  }

  @Override
  public void uncaughtException(final Thread t, final Throwable e) {
    handleException(e, t.getName());
  }

  private void handleException(final Throwable exception, final String subscriberDescription) {
    final Optional<FatalServiceFailureException> fatalServiceError =
        ExceptionUtil.getCause(exception, FatalServiceFailureException.class);

    if (fatalServiceError.isPresent()) {
      final String failedService = fatalServiceError.get().getService();
      statusLog.fatalError(failedService, exception);
      System.exit(FATAL_EXIT_CODE);
    } else if (ExceptionUtil.getCause(exception, DatabaseStorageException.class)
        .filter(DatabaseStorageException::isUnrecoverable)
        .isPresent()) {
      statusLog.fatalError(subscriberDescription, exception);
      System.exit(FATAL_EXIT_CODE);
    } else if (exception instanceof OutOfMemoryError) {
      statusLog.fatalError(subscriberDescription, exception);
      System.exit(ERROR_EXIT_CODE);
    } else if (exception instanceof ShuttingDownException) {
      LOG.debug("Shutting down", exception);
    } else if (isExpectedNettyError(exception)) {
      LOG.debug("Channel unexpectedly closed", exception);
    } else if (Throwables.getRootCause(exception) instanceof RejectedExecutionException) {
      LOG.error(
          "Unexpected rejected execution due to full task queue in {}", subscriberDescription);
    } else if (isSpecFailure(exception)) {
      statusLog.specificationFailure(subscriberDescription, exception);
    } else {
      statusLog.unexpectedFailure(subscriberDescription, exception);
    }
  }

  private boolean isExpectedNettyError(final Throwable exception) {
    return exception instanceof ClosedChannelException;
  }

  private static boolean isSpecFailure(final Throwable exception) {
    return exception instanceof IllegalArgumentException;
  }
}
