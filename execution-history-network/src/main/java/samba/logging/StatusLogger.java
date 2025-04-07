package samba.logging;

import samba.config.StartupLogConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusLogger {

  public static final StatusLogger STATUS_LOG = new StatusLogger();

  private static final Logger LOG = LoggerFactory.getLogger(StatusLogger.class);

  public void onStartup(final String version) {
    LOG.info("Samba version: {}", version);
  }

  public void startupConfigurations(final StartupLogConfig config) {
    config.getReport().forEach(LOG::info);
  }

  public void fatalError(final String description, final Throwable cause) {
    LOG.error("Exiting due to fatal error in {}", description, cause);
  }

  public void specificationFailure(final String description, final Throwable cause) {
    LOG.warn("Spec failed for {}: {}", description, cause, cause);
  }

  public void unexpectedFailure(final String description, final Throwable cause) {
    LOG.error("PLEASE FIX OR REPORT | Unexpected exception thrown for {}", description, cause);
  }
}
