package samba.services.api;

import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface PortalRestAPI {

  SafeFuture<?> start();

  SafeFuture<?> stop();

  int getListenPort();
}
