package samba.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.events.ChannelExceptionHandler;


import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.nio.channels.ClosedChannelException;

public final class PortalDefaultExceptionHandler implements ChannelExceptionHandler, UncaughtExceptionHandler {
    private static final Logger LOG = LogManager.getLogger();

    public PortalDefaultExceptionHandler() {

    }

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

    private boolean isExpectedNettyError(final Throwable exception) {
        return exception instanceof ClosedChannelException;
    }

    private static boolean isSpecFailure(final Throwable exception) {
        return exception instanceof IllegalArgumentException;
    }
}
