package samba.domain;

import org.apache.tuweni.units.bigints.UInt256;

import samba.util.IllegalPortException;

import java.net.InetAddress;

public class Node {

    private final UInt256 id;
    private final InetAddress ip;
    private final int udpPort;

    public Node(UInt256 id, InetAddress ip, int udpPort) {
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

}
