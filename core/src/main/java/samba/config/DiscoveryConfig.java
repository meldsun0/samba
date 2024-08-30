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

package samba.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.io.PortAvailability;

import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkNotNull;


public class DiscoveryConfig {
    private static final Logger LOG = LogManager.getLogger();

    public static final int DEFAULT_P2P_PORT = 9000;
    public static final int DEFAULT_P2P_PORT_IPV6 = 9090;


    private final int listenUdpPort;
    private final int listenUpdPortIpv6;
    private final List<String> bootnodes;
    private final OptionalInt advertisedUdpPort;
    private final OptionalInt advertisedUdpPortIpv6;
    private final boolean siteLocalAddressesEnabled;


    private DiscoveryConfig(
            final int listenUdpPort,
            final int listenUpdPortIpv6,
            final List<String> bootnodes,
            final OptionalInt advertisedUdpPort,
            final OptionalInt advertisedUdpPortIpv6,
            final boolean siteLocalAddressesEnabled) {

        this.listenUdpPort = listenUdpPort;
        this.listenUpdPortIpv6 = listenUpdPortIpv6;
        this.bootnodes = bootnodes;
        this.advertisedUdpPort = advertisedUdpPort;
        this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
        this.siteLocalAddressesEnabled = siteLocalAddressesEnabled;
    }

    public static Builder builder() {
        return new Builder();
    }


    public int getListenUdpPort() {
        return listenUdpPort;
    }

    public int getListenUpdPortIpv6() {
        return listenUpdPortIpv6;
    }

    public int getAdvertisedUdpPort() {
        return advertisedUdpPort.orElse(listenUdpPort);
    }

    public int getAdvertisedUdpPortIpv6() {
        return advertisedUdpPortIpv6.orElse(listenUpdPortIpv6);
    }

    public List<String> getBootnodes() {
        return bootnodes;
    }

    public boolean areSiteLocalAddressesEnabled() {
        return siteLocalAddressesEnabled;
    }

    public static class Builder {

        private OptionalInt listenUdpPort = OptionalInt.empty();
        private OptionalInt listenUdpPortIpv6 = OptionalInt.empty();
        private List<String> bootnodes;
        private OptionalInt advertisedUdpPort = OptionalInt.empty();
        private OptionalInt advertisedUdpPortIpv6 = OptionalInt.empty();
        private boolean siteLocalAddressesEnabled = false;


        private Builder() {
        }

        public DiscoveryConfig build() {
            initMissingDefaults();

            return new DiscoveryConfig(
                    listenUdpPort.orElseThrow(),
                    listenUdpPortIpv6.orElseThrow(),
                    bootnodes == null ? Collections.emptyList() : bootnodes,
                    advertisedUdpPort,
                    advertisedUdpPortIpv6,
                    siteLocalAddressesEnabled);
        }

        private void initMissingDefaults() {
            if (listenUdpPort.isEmpty()) {
                listenUdpPort = OptionalInt.of(DEFAULT_P2P_PORT);
            }
            if (listenUdpPortIpv6.isEmpty()) {
                listenUdpPortIpv6 = OptionalInt.of(DEFAULT_P2P_PORT_IPV6);
            }
        }


        public Builder listenUdpPort(final int listenUdpPort) {
            validatePort(listenUdpPort, "--p2p-udp-port");
            this.listenUdpPort = OptionalInt.of(listenUdpPort);
            return this;
        }

        public Builder listenUdpPortDefault(final int listenUdpPort) {
            validatePort(listenUdpPort, "--p2p-udp-port");
            if (this.listenUdpPort.isEmpty()) {
                this.listenUdpPort = OptionalInt.of(listenUdpPort);
            }
            return this;
        }

        public Builder listenUdpPortIpv6(final int listenUdpPortIpv6) {
            validatePort(listenUdpPortIpv6, "--p2p-udp-port-ipv6");
            this.listenUdpPortIpv6 = OptionalInt.of(listenUdpPortIpv6);
            return this;
        }

        public Builder listenUdpPortIpv6Default(final int listenUdpPortIpv6) {
            validatePort(listenUdpPortIpv6, "--p2p-udp-port-ipv6");
            if (this.listenUdpPortIpv6.isEmpty()) {
                this.listenUdpPortIpv6 = OptionalInt.of(listenUdpPortIpv6);
            }
            return this;
        }

        public Builder advertisedUdpPort(final OptionalInt advertisedUdpPort) {
            checkNotNull(advertisedUdpPort);
            if (advertisedUdpPort.isPresent()) {
                validatePort(advertisedUdpPort.getAsInt(), "--p2p-advertised-udp-port");
            }
            this.advertisedUdpPort = advertisedUdpPort;
            return this;
        }

        public Builder advertisedUdpPortDefault(final OptionalInt advertisedUdpPort) {
            checkNotNull(advertisedUdpPort);
            if (advertisedUdpPort.isPresent()) {
                validatePort(advertisedUdpPort.getAsInt(), "--p2p-advertised-udp-port");
            }
            if (this.advertisedUdpPort.isEmpty()) {
                this.advertisedUdpPort = advertisedUdpPort;
            }
            return this;
        }

        public Builder advertisedUdpPortIpv6(final OptionalInt advertisedUdpPortIpv6) {
            checkNotNull(advertisedUdpPortIpv6);
            if (advertisedUdpPortIpv6.isPresent()) {
                validatePort(advertisedUdpPortIpv6.getAsInt(), "--p2p-advertised-udp-port-ipv6");
            }
            this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
            return this;
        }

        public Builder advertisedUdpPortIpv6Default(final OptionalInt advertisedUdpPortIpv6) {
            checkNotNull(advertisedUdpPortIpv6);
            if (advertisedUdpPortIpv6.isPresent()) {
                validatePort(advertisedUdpPortIpv6.getAsInt(), "--p2p-advertised-udp-port-ipv6");
            }
            if (this.advertisedUdpPortIpv6.isEmpty()) {
                this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
            }
            return this;
        }


        public Builder bootnodes(final List<String> bootnodes) {
            checkNotNull(bootnodes);
            this.bootnodes = bootnodes;
            return this;
        }

        public Builder bootnodesDefault(final List<String> bootnodes) {
            checkNotNull(bootnodes);
            if (this.bootnodes == null) {
                this.bootnodes = bootnodes;
            }
            return this;
        }


        public Builder siteLocalAddressesEnabled(final boolean siteLocalAddressesEnabled) {
            this.siteLocalAddressesEnabled = siteLocalAddressesEnabled;
            return this;
        }

        private void validatePort(final int port, final String cliOption) {
            if (!PortAvailability.isPortValid(port)) {
                throw new InvalidConfigurationException(String.format("Invalid %s: %d", cliOption, port));
            }
        }
    }
}
