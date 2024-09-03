package samba.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.io.PortAvailability;

import java.util.ArrayList;
import java.util.List;


public class PortalRestApiConfig {

    private static final Logger LOG = LogManager.getLogger();

    public static final int DEFAULT_REST_API_PORT = 5051;
    public static final String DEFAULT_REST_API_INTERFACE = "127.0.0.1";
    public static final int DEFAULT_MAX_URL_LENGTH = 65535;
    public static final List<String> DEFAULT_REST_API_CORS_ALLOWED_ORIGINS = new ArrayList<>();
    public static final List<String> DEFAULT_REST_API_HOST_ALLOWLIST = List.of("127.0.0.1", "localhost");

    private final int restApiPort;
    private final boolean restApiDocsEnabled;
    private final String restApiInterface;
    private final int maxUrlLength;
    private final List<String> restApiCorsAllowedOrigins;
    private final List<String> restApiHostAllowlist;

    private PortalRestApiConfig(
            final int restApiPort,
            final boolean restApiDocsEnabled,
            final String restApiInterface,
            final int maxUrlLength,
            final List<String> restApiCorsAllowedOrigins,
            final List<String> restApiHostAllowlist
    ) {
        this.restApiPort = restApiPort;
        this.restApiDocsEnabled = restApiDocsEnabled;
        this.restApiInterface = restApiInterface;
        this.maxUrlLength = maxUrlLength;
        this.restApiCorsAllowedOrigins = restApiCorsAllowedOrigins;
        this.restApiHostAllowlist = restApiHostAllowlist;
    }

    public int getRestApiPort() {
        return restApiPort;
    }

    public boolean isRestApiDocsEnabled() { return restApiDocsEnabled;}

    public String getRestApiInterface() { return restApiInterface;}

    public int getMaxUrlLength() {
        return maxUrlLength;
    }

    public List<String> getRestApiCorsAllowedOrigins() {
        return restApiCorsAllowedOrigins;
    }

    public List<String> getRestApiHostAllowlist() {
        return restApiHostAllowlist;
    }

    public static PortalRestApiConfigBuilder builder() {
        return new PortalRestApiConfigBuilder();
    }

    public static final class PortalRestApiConfigBuilder {

        private int restApiPort = DEFAULT_REST_API_PORT;
        private boolean restApiDocsEnabled = false;
        private String restApiInterface = DEFAULT_REST_API_INTERFACE;
        private int maxUrlLength = DEFAULT_MAX_URL_LENGTH;
        private List<String> restApiCorsAllowedOrigins = DEFAULT_REST_API_CORS_ALLOWED_ORIGINS;
        private List<String> restApiHostAllowlist = DEFAULT_REST_API_HOST_ALLOWLIST;

        private PortalRestApiConfigBuilder() {
        }

        public PortalRestApiConfigBuilder restApiPort(final int restApiPort) {
            if (!PortAvailability.isPortValid(restApiPort)) {
                throw new InvalidConfigurationException(String.format("Invalid restApiPort: %d", restApiPort));
            }
            this.restApiPort = restApiPort;
            return this;
        }

        public PortalRestApiConfigBuilder restApiDocsEnabled(final boolean restApiDocsEnabled) {
            this.restApiDocsEnabled = restApiDocsEnabled;
            return this;
        }

        public PortalRestApiConfigBuilder restApiInterface(final String restApiInterface) {
            this.restApiInterface = restApiInterface;
            return this;
        }

        public PortalRestApiConfigBuilder maxUrlLength(final int maxUrlLength) {
            this.maxUrlLength = maxUrlLength;
            return this;
        }

        public PortalRestApiConfigBuilder restApiCorsAllowedOrigins(final List<String> restApiCorsAllowedOrigins) {
            this.restApiCorsAllowedOrigins = restApiCorsAllowedOrigins;
            return this;
        }

        public PortalRestApiConfigBuilder restApiHostAllowlist(final List<String> restApiHostAllowlist) {
            this.restApiHostAllowlist = restApiHostAllowlist;
            return this;
        }

        public PortalRestApiConfig build() {
            return new PortalRestApiConfig(
                    restApiPort,
                    restApiDocsEnabled,
                    restApiInterface,
                    maxUrlLength,
                    restApiCorsAllowedOrigins,
                    restApiHostAllowlist);
        }
    }
}