package samba.network;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Network {

    public SafeFuture<String> connect(NodeRecord peer);

    public int getPeerCount();

    public boolean isPeerConnected(NodeRecord peer);
}
