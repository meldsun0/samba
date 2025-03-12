package samba.network;

import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import samba.domain.messages.requests.Offer;

public class PortalGossip {
    public static final int MAX_GOSSIP_COUNT = 4;
    
    public static void gossip(final Network network, final Set<NodeRecord> nodes, final Bytes key) {
        List<Bytes> keyList = List.of(key);
        nodes.forEach(node -> network.offer(node, keyList, new Offer(keyList)));
    }
}
