package samba.domain.dht;

import java.util.concurrent.CompletableFuture;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public interface LivenessChecker {

  CompletableFuture<Void> checkLiveness(NodeRecord node);
}
