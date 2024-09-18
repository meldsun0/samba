package samba.network;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

import java.util.concurrent.CompletableFuture;

public interface PeerClient {


    public CompletableFuture<Bytes> sendMessage(NodeRecord nodeRecord, Bytes protocol, Bytes request);

}


