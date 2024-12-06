package samba.network;

import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Network {

  SafeFuture<String> connect(NodeRecord node);

  int getNumberOfConnectedPeers();

  boolean isNodeConnected(NodeRecord node);

  UInt256 getRadiusFromNode(NodeRecord node);
}
