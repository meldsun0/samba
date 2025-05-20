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
  public static final boolean DEFAULT_ENABLED_JSON_RPC_SERVER = true;
  public static final List<String> DEFAULT_RPC_APIS = Arrays.asList("DISCV5", "PORTAL_HISTORY");

  @Getter @Setter private boolean enableJsonRpcServer;
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

  private JsonRpcConfiguration(
      boolean enableJsonRpcServer,
      int port,
      String host,
      List<String> corsAllowedDomains,
      List<String> rpcApis,
      List<String> hostsAllowlist,
      long httpTimeoutSec,
      int maxActiveConnections,
      int maxBatchSize,
      long maxRequestContentLength,
      boolean prettyJsonEnabled) {
    this.enableJsonRpcServer = enableJsonRpcServer;
    this.port = port;
    this.host = host;
    this.corsAllowedDomains = corsAllowedDomains;
    this.rpcApis = rpcApis;
    this.hostsAllowlist = hostsAllowlist;
    this.httpTimeoutSec = httpTimeoutSec;
    this.maxActiveConnections = maxActiveConnections;
    this.maxBatchSize = maxBatchSize;
    this.maxRequestContentLength = maxRequestContentLength;
    this.prettyJsonEnabled = prettyJsonEnabled;
  }

  public static Builder builder() {
    return new Builder();
  }

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
        .add("enabled", enableJsonRpcServer)
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
    return enableJsonRpcServer == that.enableJsonRpcServer
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
        enableJsonRpcServer, port, host, corsAllowedDomains, rpcApis, hostsAllowlist, maxBatchSize);
  }

  public static class Builder {

    private boolean enableJsonRpcServer = DEFAULT_ENABLED_JSON_RPC_SERVER;
    private int port = DEFAULT_JSON_RPC_PORT;
    private String host = DEFAULT_JSON_RPC_HOST;
    private List<String> corsAllowedDomains = Collections.emptyList();
    private List<String> rpcApis = DEFAULT_RPC_APIS;
    private List<String> hostsAllowlist = Arrays.asList("localhost", "127.0.0.1");
    private long httpTimeoutSec = TimeoutOptions.defaultOptions().getTimeoutSeconds();
    private int maxActiveConnections = DEFAULT_MAX_ACTIVE_CONNECTIONS;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private long maxRequestContentLength = DEFAULT_MAX_REQUEST_CONTENT_LENGTH;
    private boolean prettyJsonEnabled = DEFAULT_PRETTY_JSON_ENABLED;

    private Builder() {}

    public JsonRpcConfiguration build() {
      return new JsonRpcConfiguration(
          this.enableJsonRpcServer,
          this.port,
          this.host,
          this.corsAllowedDomains,
          this.rpcApis,
          this.hostsAllowlist,
          this.httpTimeoutSec,
          this.maxActiveConnections,
          this.maxBatchSize,
          this.maxRequestContentLength,
          this.prettyJsonEnabled);
    }

    public Builder enableJsonRpcServer(boolean enableJsonRpcServer) {
      this.enableJsonRpcServer = enableJsonRpcServer;
      return this;
    }

    public Builder port(int port) {
      this.port = port;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder corsAllowedDomains(List<String> corsAllowedDomains) {
      this.corsAllowedDomains = corsAllowedDomains;
      return this;
    }

    public Builder rpcApis(List<String> rpcApis) {
      this.rpcApis = rpcApis;
      return this;
    }

    public Builder hostsAllowlist(List<String> hostsAllowlist) {
      this.hostsAllowlist = hostsAllowlist;
      return this;
    }

    public Builder httpTimeoutSec(long httpTimeoutSec) {
      this.httpTimeoutSec = httpTimeoutSec;
      return this;
    }

    public Builder maxActiveConnections(int maxActiveConnections) {
      this.maxActiveConnections = maxActiveConnections;
      return this;
    }

    public Builder maxBatchSize(int maxBatchSize) {
      this.maxBatchSize = maxBatchSize;
      return this;
    }

    public Builder maxRequestContentLength(long maxRequestContentLength) {
      this.maxRequestContentLength = maxRequestContentLength;
      return this;
    }

    public Builder prettyJsonEnabled(boolean prettyJsonEnabled) {
      this.prettyJsonEnabled = prettyJsonEnabled;
      return this;
    }
  }

  public List<String> getJsonRpcServerSummaryLog() {
    List<String> summary = new ArrayList<>();
    summary.add("Json-RPC Server Summary:");
    if (!this.enableJsonRpcServer) {
      summary.add("Enable: false");
    } else {
      summary.add(
          String.format(
              "Enabled: true, Listen Address: %s, Port: %s, APIS: %s, Pretty: %s",
              this.host, this.port, String.join(",", this.rpcApis), this.prettyJsonEnabled));
      summary.add(
          String.format(
              "Allow: %s, CORS: %s",
              String.join(",", this.hostsAllowlist), String.join(",", this.corsAllowedDomains)));
      summary.add(
          String.format(
              "Timeout: %s, Max Active Connections: %s",
              this.httpTimeoutSec, this.maxActiveConnections));
      summary.add(
          String.format(
              "Max Batch size: %s, Max Request Content Length: %s",
              this.maxBatchSize, this.maxRequestContentLength));
    }
    return summary;
  }
}
