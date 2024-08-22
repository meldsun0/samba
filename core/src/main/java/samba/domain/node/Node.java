package samba.domain.node;

import lombok.Getter;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.crypto.Hash;

import samba.util.IllegalPortException;

import java.net.InetAddress;
import java.util.Objects;

/**
 *
 */
public class Node {

    @Getter
    private final Bytes id;
    private final InetAddress ip;
    private final int udpPort;
    private Bytes32 keccak256;

    public Node(Bytes id, InetAddress ip, int udpPort) {
        checkPort(udpPort, "UDP");
        this.id = id;
        this.ip = ip;
        this.udpPort = udpPort;
    }

    public static void checkPort(final int port, final String portTypeName) {
        if (!(port > 0 && port < 65536)) {
            throw new IllegalPortException(String.format("%s port requires a value between 1 and 65535. Got %d.", portTypeName, port));
        }
    }

    public Bytes32 keccak256() {
        if (keccak256 == null) {
            keccak256 = Hash.keccak256(getId());
        }
        return keccak256;
    }


    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || o.getClass().isAssignableFrom(this.getClass())) return false;
        final Node that = (Node) o;
        return Objects.equals(id, that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
