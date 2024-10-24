package samba.domain.routingtable;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import java.util.concurrent.CompletableFuture;

public interface LivenessChecker {

    CompletableFuture<Void> checkLiveness(NodeRecord node);
}
