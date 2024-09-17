package samba.network;


import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;


import java.util.concurrent.CompletableFuture;

public interface PeerClient<T> {


    CompletableFuture<T> sendMessage(NodeRecord nodeRecord, Bytes protocol, Bytes request);
}


//  public CompletableFuture<Bytes> talk(NodeRecord nodeRecord, Bytes protocol, Bytes request) {