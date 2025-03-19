/*
 * Copyright Consensys Software Inc., 2022
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package samba.logging;

import samba.config.StartupLogConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatusLogger {

  public static final StatusLogger STATUS_LOG = new StatusLogger("samba-status-log");

  final Logger log;

  private StatusLogger(final String name) {
    this.log = LogManager.getLogger(name);
  }

  public void onStartup(final String version) {
    log.info("Samba version: {}", version);
  }

  public void startupConfigurations(final StartupLogConfig config) {
    config.getReport().forEach(log::info);
  }

  public void fatalError(final String description, final Throwable cause) {
    log.fatal("Exiting due to fatal error in {}", description, cause);
  }

  public void specificationFailure(final String description, final Throwable cause) {
    log.warn("Spec failed for {}: {}", description, cause, cause);
  }

  public void unexpectedFailure(final String description, final Throwable cause) {
    log.error("PLEASE FIX OR REPORT | Unexpected exception thrown for {}", description, cause);
  }
}
