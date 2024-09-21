package samba.network;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Network {

    public SafeFuture<NodeRecord> connect(NodeRecord peer);

    public int getPeerCount();
}
