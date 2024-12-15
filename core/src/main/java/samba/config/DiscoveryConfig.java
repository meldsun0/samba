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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.net.InetAddresses.isInetAddress;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.web3j.utils.Strings;
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;
import tech.pegasys.teku.infrastructure.io.PortAvailability;

public class DiscoveryConfig {
  private static final Logger LOG = LogManager.getLogger();

  public static final int DEFAULT_UDP_PORT = 9000;
  public static final int DEFAULT_UDP_PORT_IPV6 = 9090;
  public static final List<String> DEFAULT_P2P_INTERFACE = List.of("0.0.0.0");

  // udp
  private final int listenUdpPort;
  private final int listenUdpPortIpv6;
  private final OptionalInt advertisedUdpPort;
  private final OptionalInt advertisedUdpPortIpv6;
  // tcp
  private final int listenPort;
  private final int listenPortIpv6;
  private final OptionalInt advertisedPort;
  private final OptionalInt advertisedPortIpv6;

  private final List<NodeRecord> bootnodes;
  private final List<String> networkInterfaces;
  private final Optional<List<String>> advertisedIps;
  private final String clientValue = "a";
  private final String clientKey = "c";

  private DiscoveryConfig(
      final int listenUdpPort,
      final int listenUdpPortIpv6,
      final OptionalInt advertisedUdpPort,
      final OptionalInt advertisedUdpPortIpv6,
      final int listenPort,
      final int listenPortIpv6,
      final OptionalInt advertisedPort,
      final OptionalInt advertisedPortIpv6,
      final List<NodeRecord> bootnodes,
      final List<String> networkInterfaces,
      final Optional<List<String>> advertisedIps) {
    // udp
    this.listenUdpPort = listenUdpPort;
    this.listenUdpPortIpv6 = listenUdpPortIpv6;
    this.advertisedUdpPort = advertisedUdpPort;
    this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
    // tcp
    this.listenPort = listenPort;
    this.listenPortIpv6 = listenPortIpv6;
    this.advertisedPort = advertisedPort;
    this.advertisedPortIpv6 = advertisedPortIpv6;

    this.bootnodes = bootnodes;
    this.networkInterfaces = networkInterfaces;
    this.advertisedIps = advertisedIps;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getListenPort() {
    return listenPort;
  }

  public int getListenPortIpv6() {
    return listenPortIpv6;
  }

  public int getAdvertisedPort() {
    return advertisedPort.orElse(listenPort);
  }

  public int getAdvertisedPortIpv6() {
    return advertisedPortIpv6.orElse(listenPortIpv6);
  }

  public int getListenUdpPort() {
    return listenUdpPort;
  }

  public List<String> getAdvertisedIps() {
    return advertisedIps.orElse(networkInterfaces).stream()
        .map(this::resolveAnyLocalAddress)
        .toList();
  }

  public int getListenUdpPortIpv6() {
    return listenUdpPortIpv6;
  }

  public int getAdvertisedUdpPort() {
    return advertisedUdpPort.orElse(listenUdpPort);
  }

  public int getAdvertisedUdpPortIpv6() {
    return advertisedUdpPortIpv6.orElse(listenUdpPortIpv6);
  }

  public List<String> getNetworkInterfaces() {
    return networkInterfaces;
  }

  public String getClientKey() {
    return this.clientKey;
  }

  public Bytes getClientValue() {
    return Bytes.wrap(this.clientValue.getBytes());
  }

  public List<NodeRecord> getBootnodes() {
    return bootnodes;
  }

  private String resolveAnyLocalAddress(final String ipAddress) {
    try {
      final InetAddress advertisedAddress = InetAddress.getByName(ipAddress);
      if (advertisedAddress.isAnyLocalAddress()) {
        final IPVersionResolver.IPVersion ipVersion = IPVersionResolver.resolve(advertisedAddress);
        return getLocalAddress(ipVersion);
      } else {
        return ipAddress;
      }
    } catch (final UnknownHostException ex) {
      LOG.error("Failed resolving local address: {}. Trying to use {}", ex.getMessage(), ipAddress);
      return ipAddress;
    }
  }

  private String getLocalAddress(final IPVersionResolver.IPVersion ipVersion)
      throws UnknownHostException {
    try {
      final InetAddress localHostAddress = InetAddress.getLocalHost();
      if (localHostAddress.isAnyLocalAddress()
          && IPVersionResolver.resolve(localHostAddress) == ipVersion) {
        return localHostAddress.getHostAddress();
      }
      final Enumeration<NetworkInterface> networkInterfaces =
          NetworkInterface.getNetworkInterfaces();
      while (networkInterfaces.hasMoreElements()) {
        final NetworkInterface networkInterface = networkInterfaces.nextElement();
        final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
        while (inetAddresses.hasMoreElements()) {
          final InetAddress inetAddress = inetAddresses.nextElement();
          if (IPVersionResolver.resolve(inetAddress) != ipVersion) {
            // incompatible IP version
            continue;
          }
          switch (ipVersion) {
            case IP_V4 -> {
              // IPv4 (include only site local addresses)
              if (inetAddress.isSiteLocalAddress()) {
                return inetAddress.getHostAddress();
              }
            }
            case IP_V6 -> {
              // IPv6 (include site local or unique local addresses)
              if (inetAddress.isSiteLocalAddress() || isUniqueLocalAddress(inetAddress)) {
                return inetAddress.getHostAddress();
              }
            }
          }
        }
      }
    } catch (final SocketException ex) {
      LOG.error("Failed to find local address", ex);
      throw new UnknownHostException(ex.getMessage());
    }
    throw new UnknownHostException(
        String.format("Unable to determine local %s Address", ipVersion.getName()));
  }

  private boolean isUniqueLocalAddress(final InetAddress inetAddress) {
    // Check the first byte to determine if it's in the fc00::/7 range
    // Unique local IPv6 addresses start with 0xfc or 0xfd
    final int firstByte = inetAddress.getAddress()[0] & 0xff; // Convert to unsigned
    return (firstByte == 0xfc || firstByte == 0xfd);
  }

  public int getAdvertisedUdpPort(IPVersionResolver.IPVersion ipVersion) {
    return switch (ipVersion) {
      case IP_V4 -> this.getAdvertisedUdpPort();
      case IP_V6 -> this.getAdvertisedUdpPortIpv6();
      default -> throw new IllegalArgumentException("Unsupported IP version for UDP port");
    };
  }

  public int getAdvertisedTcpPort(IPVersionResolver.IPVersion ipVersion) {
    return switch (ipVersion) {
      case IP_V4 -> this.getAdvertisedPort();
      case IP_V6 -> this.getAdvertisedPortIpv6();
      default -> throw new IllegalArgumentException("Unsupported IP version for TCP port");
    };
  }

  public static class Builder {

    // udp
    private OptionalInt listenUdpPort = OptionalInt.empty();
    private OptionalInt listenUdpPortIpv6 = OptionalInt.empty();
    private OptionalInt advertisedUdpPort = OptionalInt.empty();
    private OptionalInt advertisedUdpPortIpv6 = OptionalInt.empty();

    // tcp
    private int listenPort = DEFAULT_UDP_PORT;
    private int listenPortIpv6 = DEFAULT_UDP_PORT_IPV6;
    private OptionalInt advertisedPort = OptionalInt.empty();
    private OptionalInt advertisedPortIpv6 = OptionalInt.empty();

    private List<NodeRecord> bootnodes = List.of();
    private List<String> networkInterfaces = List.of();
    private Optional<List<String>> advertisedIps = Optional.empty();

    private Builder() {}

    public DiscoveryConfig build() {
      initMissingDefaults();

      return new DiscoveryConfig(
          listenUdpPort.orElseThrow(),
          listenUdpPortIpv6.orElseThrow(),
          advertisedUdpPort,
          advertisedUdpPortIpv6,
          listenPort,
          listenPortIpv6,
          advertisedPort,
          advertisedPortIpv6,
          bootnodes,
          networkInterfaces,
          advertisedIps);
    }

    private void initMissingDefaults() {
      if (listenUdpPort.isEmpty()) {
        listenUdpPort = OptionalInt.of(DEFAULT_UDP_PORT);
      }
      if (listenUdpPortIpv6.isEmpty()) {
        listenUdpPortIpv6 = OptionalInt.of(DEFAULT_UDP_PORT_IPV6);
      }
      if (networkInterfaces.isEmpty()) {
        networkInterfaces = DEFAULT_P2P_INTERFACE;
      }
    }

    public Builder advertisedIp(final Optional<String> advertisedIp) {
      return advertisedIps(advertisedIp.map(Collections::singletonList));
    }

    public Builder advertisedIps(final Optional<List<String>> advertisedIps) {
      checkNotNull(advertisedIps);
      advertisedIps.ifPresent(
          ips -> {
            ips.forEach(
                ip -> {
                  if (Strings.isBlank(ip)) {
                    throw new InvalidConfigurationException("Advertised ip is blank");
                  }
                  if (!isInetAddress(ip)) {
                    throw new InvalidConfigurationException(
                        String.format("Advertised ip (%s) is set incorrectly", ip));
                  }
                });
            validateAddresses(ips, "--p2p-advertised-ip");
          });
      this.advertisedIps = advertisedIps;
      return this;
    }

    public Builder listenUdpPort(final int listenUdpPort) {
      validatePort(listenUdpPort, "--udp-port");
      this.listenUdpPort = OptionalInt.of(listenUdpPort);
      return this;
    }

    public Builder listenUdpPortDefault(final int listenUdpPort) {
      validatePort(listenUdpPort, "--udp-port");
      if (this.listenUdpPort.isEmpty()) {
        this.listenUdpPort = OptionalInt.of(listenUdpPort);
      }
      return this;
    }

    public Builder listenUdpPortIpv6(final int listenUdpPortIpv6) {
      validatePort(listenUdpPortIpv6, "-udp-port-ipv6");
      this.listenUdpPortIpv6 = OptionalInt.of(listenUdpPortIpv6);
      return this;
    }

    public Builder listenUdpPortIpv6Default(final int listenUdpPortIpv6) {
      validatePort(listenUdpPortIpv6, "--udp-port-ipv6");
      if (this.listenUdpPortIpv6.isEmpty()) {
        this.listenUdpPortIpv6 = OptionalInt.of(listenUdpPortIpv6);
      }
      return this;
    }

    public Builder advertisedUdpPort(final OptionalInt advertisedUdpPort) {
      checkNotNull(advertisedUdpPort);
      if (advertisedUdpPort.isPresent()) {
        validatePort(advertisedUdpPort.getAsInt(), "--advertised-udp-port");
      }
      this.advertisedUdpPort = advertisedUdpPort;
      return this;
    }

    public Builder advertisedUdpPortDefault(final OptionalInt advertisedUdpPort) {
      checkNotNull(advertisedUdpPort);
      if (advertisedUdpPort.isPresent()) {
        validatePort(advertisedUdpPort.getAsInt(), "--advertised-udp-port");
      }
      if (this.advertisedUdpPort.isEmpty()) {
        this.advertisedUdpPort = advertisedUdpPort;
      }
      return this;
    }

    public Builder advertisedUdpPortIpv6(final OptionalInt advertisedUdpPortIpv6) {
      checkNotNull(advertisedUdpPortIpv6);
      if (advertisedUdpPortIpv6.isPresent()) {
        validatePort(advertisedUdpPortIpv6.getAsInt(), "-advertised-udp-port-ipv6");
      }
      this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
      return this;
    }

    public Builder advertisedUdpPortIpv6Default(final OptionalInt advertisedUdpPortIpv6) {
      checkNotNull(advertisedUdpPortIpv6);
      if (advertisedUdpPortIpv6.isPresent()) {
        validatePort(advertisedUdpPortIpv6.getAsInt(), "-advertised-udp-port-ipv6");
      }
      if (this.advertisedUdpPortIpv6.isEmpty()) {
        this.advertisedUdpPortIpv6 = advertisedUdpPortIpv6;
      }
      return this;
    }

    public Builder networkInterfaces(final List<String> networkInterfaces) {
      checkNotNull(networkInterfaces);
      validateAddresses(networkInterfaces, "--interface");
      this.networkInterfaces = networkInterfaces;
      return this;
    }

    public Builder bootnodes(final List<NodeRecord> bootnodes) {
      checkNotNull(bootnodes);
      this.bootnodes = bootnodes;
      return this;
    }

    public Builder bootnodesDefault(final List<NodeRecord> bootnodes) {
      checkNotNull(bootnodes);
      if (this.bootnodes == null) {
        this.bootnodes = bootnodes;
      }
      return this;
    }

    private void validatePort(final int port, final String cliOption) {
      if (!PortAvailability.isPortValid(port)) {
        throw new InvalidConfigurationException(String.format("Invalid %s: %d", cliOption, port));
      }
    }

    private void validateAddresses(final List<String> addresses, final String cliOption) {
      checkState(
          addresses.size() == 1 || addresses.size() == 2,
          "Invalid number of %s. It should be either 1 or 2, but it was %s",
          cliOption,
          addresses.size());
      if (addresses.size() == 2) {
        final Set<IPVersionResolver.IPVersion> ipVersions =
            addresses.stream().map(IPVersionResolver::resolve).collect(Collectors.toSet());
        if (ipVersions.size() != 2) {
          throw new InvalidConfigurationException(
              String.format(
                  "Expected an IPv4 and an IPv6 address for %s but only %s was set",
                  cliOption, ipVersions));
        }
      }
    }
  }
}
