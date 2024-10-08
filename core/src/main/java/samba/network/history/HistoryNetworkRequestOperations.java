package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Nodes;
import samba.domain.messages.response.Pong;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.Optional;


public interface HistoryNetworkRequestOperations {

    SafeFuture<Optional<Pong>> ping(NodeRecord nodeRecord, Ping message);


     SafeFuture<Optional<Nodes>> findNodes(NodeRecord nodeRecord, FindNodes findNodes);
    

}
