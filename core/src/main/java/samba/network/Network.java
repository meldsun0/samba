package samba.network;

import samba.domain.messages.response.Pong;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface Network {

  SafeFuture<Optional<Pong>> ping(NodeRecord node);

  int getNumberOfConnectedPeers();

  boolean isNodeConnected(NodeRecord node);

  UInt256 getRadiusFromNode(NodeRecord node);
}
