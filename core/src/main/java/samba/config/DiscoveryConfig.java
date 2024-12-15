package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

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
import tech.pegasys.teku.infrastructure.io.IPVersionResolver;
import tech.pegasys.teku.infrastructure.io.PortAvailability;

public class DiscoveryConfig {
  private static final Logger LOG = LogManager.getLogger();

  public static final int DEFAULT_UDP_PORT_IPV4 = 9000;
  public static final int DEFAULT_UDP_PORT_IPV6 = 9090;

  public static final int DEFAULT_TCP_PORT_IPV4 = 9000;
  public static final int DEFAULT_TCP_PORT_IPV6 = 9090;

  public static final List<String> DEFAULT_P2P_INTERFACE = List.of("0.0.0.0");

  private final int listenUDPPortIpv4;
  private final int listenTCPPortIpv4;

  private final int listenUDPPortIpv6;
  private final int listenTCPPortIpv6;

  private final List<NodeRecord> bootnodes;
  private final List<String> networkInterfaces;

  private final String clientValue = "a";
  private final String clientKey = "c";

  private DiscoveryConfig(
      final int listenUDPPortIpv4,
      final int listenTCPPortIpv4,
      final int listenUDPPortIpv6,
      final int listenTCPPortIpv6,
      final List<NodeRecord> bootnodes,
      final List<String> networkInterfaces) {

    this.listenUDPPortIpv4 = listenUDPPortIpv4;
    this.listenTCPPortIpv4 = listenTCPPortIpv4;
    this.listenUDPPortIpv6 = listenUDPPortIpv6;
    this.listenTCPPortIpv6 = listenTCPPortIpv6;

    this.bootnodes = bootnodes;
    this.networkInterfaces = networkInterfaces;
  }

  public static Builder builder() {
    return new Builder();
  }

  public int getListenUDPPortIpv4() {
    return listenUDPPortIpv4;
  }

  public int getListenTCPPortIpv4() {
    return listenTCPPortIpv4;
  }

  public int getListenUDPPortIpv6() {
    return listenUDPPortIpv6;
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

  public List<String> getIps() {
    return networkInterfaces.stream().map(this::resolveAnyLocalAddress).toList();
  }

  public int getUDPPort(IPVersionResolver.IPVersion ipVersion) {
    return switch (ipVersion) {
      case IP_V4 -> this.listenUDPPortIpv4;
      case IP_V6 -> this.listenUDPPortIpv6;
      default -> throw new IllegalArgumentException("Unsupported IP version for UDP port");
    };
  }

  public int getTCPPort(IPVersionResolver.IPVersion ipVersion) {
    return switch (ipVersion) {
      case IP_V4 -> this.listenTCPPortIpv4;
      case IP_V6 -> this.listenTCPPortIpv6;
      default -> throw new IllegalArgumentException("Unsupported IP version for TCP port");
    };
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

  public static class Builder {

    private int listenUDPPortIPv4 = DEFAULT_UDP_PORT_IPV4;
    private int listenTCPPortIPv4 = DEFAULT_TCP_PORT_IPV4;
    private int listenUDPPortIpv6 = DEFAULT_UDP_PORT_IPV6;
    private int listenTCPPortIpv6 = DEFAULT_TCP_PORT_IPV6;

    private List<NodeRecord> bootnodes = List.of();
    private List<String> networkInterfaces = DEFAULT_P2P_INTERFACE;

    private Builder() {}

    public DiscoveryConfig build() {
      return new DiscoveryConfig(
          listenUDPPortIPv4,
          listenUDPPortIpv6,
          listenTCPPortIPv4,
          listenTCPPortIpv6,
          bootnodes,
          networkInterfaces);
    }

    public Builder networkInterfaces(final List<String> networkInterfaces) {
      checkNotNull(networkInterfaces);
      validateAddresses(networkInterfaces, "--p2p-ip, --p2p-ip-ips");
      this.networkInterfaces = networkInterfaces;
      return this;
    }

    public Builder bootnodes(final List<NodeRecord> bootnodes) {
      checkNotNull(bootnodes);
      this.bootnodes = bootnodes;
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
