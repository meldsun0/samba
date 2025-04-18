package samba.jsonrpc.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;

@SuppressWarnings("SameNameButDifferent")
public class JsonRpcConfiguration {
  public static final String DEFAULT_JSON_RPC_HOST = "127.0.0.1";
  public static final int DEFAULT_JSON_RPC_PORT = 8545;

  public static final int DEFAULT_MAX_ACTIVE_CONNECTIONS = 80;
  public static final int DEFAULT_MAX_BATCH_SIZE = 1024;
  public static final long DEFAULT_MAX_REQUEST_CONTENT_LENGTH = 5 * 1024 * 1024; // 5MB
  public static final boolean DEFAULT_PRETTY_JSON_ENABLED = false;
  public static final List<String> DEFAULT_RPC_APIS = Arrays.asList("DISCV5", "PORTAL_HISTORY");

  @Getter @Setter private boolean enabled;
  @Getter @Setter private int port;
  @Getter @Setter private String host;
  private List<String> corsAllowedDomains = Collections.emptyList();
  @Getter @Setter private List<String> rpcApis;
  @Setter private List<String> hostsAllowlist = Arrays.asList("localhost", "127.0.0.1");
  @Getter @Setter private long httpTimeoutSec = TimeoutOptions.defaultOptions().getTimeoutSeconds();
  @Getter @Setter private int maxActiveConnections;
  @Getter @Setter private int maxBatchSize;
  @Getter @Setter private long maxRequestContentLength;
  @Setter @Getter private boolean prettyJsonEnabled;

  public static JsonRpcConfiguration createDefault() {
    final JsonRpcConfiguration config = new JsonRpcConfiguration();
    config.setEnabled(true);
    config.setPort(DEFAULT_JSON_RPC_PORT);
    config.setHost(DEFAULT_JSON_RPC_HOST);
    config.setRpcApis(DEFAULT_RPC_APIS);
    config.httpTimeoutSec = TimeoutOptions.defaultOptions().getTimeoutSeconds();
    config.setMaxActiveConnections(DEFAULT_MAX_ACTIVE_CONNECTIONS);
    config.setMaxBatchSize(DEFAULT_MAX_BATCH_SIZE);
    config.setMaxRequestContentLength(DEFAULT_MAX_REQUEST_CONTENT_LENGTH);
    config.setPrettyJsonEnabled(DEFAULT_PRETTY_JSON_ENABLED);
    return config;
  }

  private JsonRpcConfiguration() {}

  public Collection<String> getCorsAllowedDomains() {
    return corsAllowedDomains;
  }

  public void setCorsAllowedDomains(final List<String> corsAllowedDomains) {
    if (corsAllowedDomains != null) {
      this.corsAllowedDomains = corsAllowedDomains;
    }
  }

  public void addRpcApi(final String rpcApi) {
    this.rpcApis = new ArrayList<>(rpcApis);
    rpcApis.add(rpcApi);
  }

  public Collection<String> getHostsAllowlist() {
    return Collections.unmodifiableCollection(this.hostsAllowlist);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("enabled", enabled)
        .add("port", port)
        .add("host", host)
        .add("corsAllowedDomains", corsAllowedDomains)
        .add("hostsAllowlist", hostsAllowlist)
        .add("rpcApis", rpcApis)
        .add("httpTimeoutSec", httpTimeoutSec)
        .add("maxActiveConnections", maxActiveConnections)
        .add("maxBatchSize", maxBatchSize)
        .toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final JsonRpcConfiguration that = (JsonRpcConfiguration) o;
    return enabled == that.enabled
        && port == that.port
        && Objects.equals(host, that.host)
        && Objects.equals(corsAllowedDomains, that.corsAllowedDomains)
        && Objects.equals(rpcApis, that.rpcApis)
        && Objects.equals(hostsAllowlist, that.hostsAllowlist)
        && maxBatchSize == that.maxBatchSize;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        enabled, port, host, corsAllowedDomains, rpcApis, hostsAllowlist, maxBatchSize);
  }
}
