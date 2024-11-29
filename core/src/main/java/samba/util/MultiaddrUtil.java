package samba.util;

import io.libp2p.core.multiformats.Multiaddr;
import io.libp2p.core.multiformats.Protocol;
import org.apache.tuweni.bytes.Bytes;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class MultiaddrUtil {

    static Multiaddr fromInetSocketAddress(final InetSocketAddress address, final String protocol) {
        final String addrString =
                String.format(
                        "/%s/%s/%s/%d",
                        protocol(address.getAddress()),
                        address.getAddress().getHostAddress(),
                        protocol,
                        address.getPort());
        return Multiaddr.fromString(addrString);
    }

    public static Bytes getMultiAddrValue(Multiaddr multiaddr, Protocol protocol) {
        return Bytes.wrap((multiaddr.getFirstComponent(protocol).getValue()));
    }

    private static String protocol(final InetAddress address) {
        return address instanceof Inet6Address ? "ip6" : "ip4";
    }
}
