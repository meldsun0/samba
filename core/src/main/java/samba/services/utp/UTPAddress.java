package samba.services.utp;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.utp.network.TransportAddress;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class UTPAddress implements TransportAddress {

    private NodeRecord nodeRecord;
    private int port;
    private String remoteAddress;


    public UTPAddress(final String remoteAddress, int port, NodeRecord nodeRecord) {
        this.port = port;
        this.remoteAddress = remoteAddress;
        this.nodeRecord = nodeRecord;
    }

    @Override
    public NodeRecord getAddress() {
        return this.nodeRecord;
    }

    public int getPort() {
        return this.port;
    }
}
