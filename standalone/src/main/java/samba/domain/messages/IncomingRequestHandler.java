package samba.domain.messages;

import samba.network.NetworkType;

import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface IncomingRequestHandler {

  CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request);

  NetworkType getNetworkType();
}
