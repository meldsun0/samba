package samba.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.events.ChannelExceptionHandler;

public final class PortalDefaultExceptionHandler
    implements ChannelExceptionHandler, UncaughtExceptionHandler {
  private static final Logger LOG = LogManager.getLogger();

  public PortalDefaultExceptionHandler() {}

  @Override
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
    LOG.debug("Shutting down", exception);
  }
}
