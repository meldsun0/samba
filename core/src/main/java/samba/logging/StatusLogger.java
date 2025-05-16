package samba.logging;

import samba.config.StartupLogConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusLogger {

  public static final StatusLogger STATUS_LOG = new StatusLogger("samba-status-log");

  final Logger log;

  private StatusLogger(final String name) {
    this.log = LoggerFactory.getLogger(name);
  }

  public void onStartup(final String version) {
    log.info("Samba version: {}", version);
  }

  public void startupConfigurations(final StartupLogConfig config) {
    config.getReport().forEach(log::info);
  }

  public void specificationFailure(final String description, final Throwable cause) {
    log.warn("Spec failed for {}: {}", description, cause, cause);
  }

  public void unexpectedFailure(final String description, final Throwable cause) {
    log.error("PLEASE FIX OR REPORT | Unexpected exception thrown for {}", description, cause);
  }
}
