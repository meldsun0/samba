package samba.config;

import java.util.List;

import lombok.Getter;
import tech.pegasys.teku.infrastructure.io.PortAvailability;

@Getter
public class RestServerConfig {

  public static final int DEFAULT_REST_API_PORT = 5051;
  public static final String DEFAULT_REST_API_INTERFACE = "0.0.0.0";
  public static final int DEFAULT_MAX_URL_LENGTH = 65535;
  public static final List<String> DEFAULT_REST_API_CORS_ALLOWED_ORIGINS =
      List.of("127.0.0.1", "localhost");
  public static final List<String> DEFAULT_REST_API_HOST_ALLOWLIST =
      List.of("127.0.0.1", "localhost", "0.0.0.0");
  public static final boolean DEFAULT_ENABLED_REST_SERVER = true;

  private final int restApiPort;
  private final boolean restApiDocsEnabled;
  private final boolean enableRestServer;
  private final String restApiInterface;
  private final int maxUrlLength;
  private final List<String> restApiCorsAllowedOrigins;
  private final List<String> restApiHostAllowlist;

  private RestServerConfig(
      final boolean enableRestServer,
      final int restApiPort,
      final boolean restApiDocsEnabled,
      final String restApiInterface,
      final int maxUrlLength,
      final List<String> restApiCorsAllowedOrigins,
      final List<String> restApiHostAllowlist) {
    this.enableRestServer = enableRestServer;
    this.restApiPort = restApiPort;
    this.restApiDocsEnabled = restApiDocsEnabled;
    this.restApiInterface = restApiInterface;
    this.maxUrlLength = maxUrlLength;
    this.restApiCorsAllowedOrigins = restApiCorsAllowedOrigins;
    this.restApiHostAllowlist = restApiHostAllowlist;
  }

  public boolean isRestServerEnabled() {
    return this.enableRestServer;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private int restApiPort = DEFAULT_REST_API_PORT;
    private boolean enableRestServer = DEFAULT_ENABLED_REST_SERVER;
    private boolean restApiDocsEnabled = true;
    private String restApiInterface = DEFAULT_REST_API_INTERFACE;
    private int maxUrlLength = DEFAULT_MAX_URL_LENGTH;
    private List<String> restApiCorsAllowedOrigins = DEFAULT_REST_API_CORS_ALLOWED_ORIGINS;
    private List<String> restApiHostAllowlist = DEFAULT_REST_API_HOST_ALLOWLIST;

    private Builder() {}

    public Builder enableRestServer(final boolean enableRestServer) {
      this.enableRestServer = enableRestServer;
      return this;
    }

    public Builder restApiPort(final int restApiPort) {
      if (!PortAvailability.isPortValid(restApiPort)) {
        throw new InvalidConfigurationException(
            String.format("Invalid restApiPort: %d", restApiPort));
      }
      this.restApiPort = restApiPort;
      return this;
    }

    public Builder restApiDocsEnabled(final boolean restApiDocsEnabled) {
      this.restApiDocsEnabled = restApiDocsEnabled;
      return this;
    }

    public Builder restApiInterface(final String restApiInterface) {
      this.restApiInterface = restApiInterface;
      return this;
    }

    public Builder maxUrlLength(final int maxUrlLength) {
      this.maxUrlLength = maxUrlLength;
      return this;
    }

    public Builder restApiCorsAllowedOrigins(final List<String> restApiCorsAllowedOrigins) {
      this.restApiCorsAllowedOrigins = restApiCorsAllowedOrigins;
      return this;
    }

    public Builder restApiHostAllowlist(final List<String> restApiHostAllowlist) {
      this.restApiHostAllowlist = restApiHostAllowlist;
      return this;
    }

    public RestServerConfig build() {
      return new RestServerConfig(
          enableRestServer,
          restApiPort,
          restApiDocsEnabled,
          restApiInterface,
          maxUrlLength,
          restApiCorsAllowedOrigins,
          restApiHostAllowlist);
    }
  }
}
