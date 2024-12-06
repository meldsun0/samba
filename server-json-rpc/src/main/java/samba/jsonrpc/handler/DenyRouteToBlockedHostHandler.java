package samba.jsonrpc.handler;

import samba.jsonrpc.config.JsonRpcConfiguration;

import java.util.Optional;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.net.HostAndPort;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DenyRouteToBlockedHostHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(DenyRouteToBlockedHostHandler.class);
  private final JsonRpcConfiguration config;

  public DenyRouteToBlockedHostHandler(JsonRpcConfiguration config) {
    this.config = config;
  }

  @Override
  public void handle(RoutingContext event) {
    final Optional<String> hostHeader = getAndValidateHostHeader(event);
    if (config.getHostsAllowlist().contains("*")
        || (hostHeader.isPresent() && hostIsInAllowlist(hostHeader.get()))) {
      event.next();
    } else {
      final HttpServerResponse response = event.response();
      if (!response.closed()) {
        response
            .setStatusCode(403)
            .putHeader("Content-Type", "application/json; charset=utf-8")
            .end("{\"message\":\"Host not authorized.\"}");
      }
    }
  }

  private Optional<String> getAndValidateHostHeader(final RoutingContext event) {
    final HostAndPort hostAndPort = event.request().authority();
    return Optional.ofNullable(hostAndPort).map(HostAndPort::host);
  }

  private boolean hostIsInAllowlist(final String hostHeader) {
    if (config.getHostsAllowlist().contains("*")
        || config.getHostsAllowlist().stream()
            .anyMatch(allowlistEntry -> allowlistEntry.equalsIgnoreCase(hostHeader))) {
      return true;
    } else {
      LOG.trace("Host not in allowlist: '{}'", hostHeader);
      return false;
    }
  }
}
