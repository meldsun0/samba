package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5GetRoutingTable {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5GetRoutingTable.class);
  public static final int MAX_ROUTING_TABLE_SIZE = 16;

  private final Discv5Client discv5Client;

  public Discv5GetRoutingTable(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<List<List<String>>> execute() {
    List<List<NodeRecord>> routingTable = discv5Client.getRoutingTable();
    return Optional.of(
        routingTable.stream()
            .map(
                innerList -> {
                  List<String> reversed =
                      innerList.stream()
                          .map(NodeRecord::getNodeId)
                          .map(Bytes::toHexString)
                          .limit(MAX_ROUTING_TABLE_SIZE)
                          .collect(Collectors.toList());
                  Collections.reverse(
                      reversed); // ordered from least-recently connected to most-recently
                  // connected.
                  return reversed;
                })
            .collect(Collectors.toList()));
  }

  public static Optional<List<List<String>>> execute(final Discv5Client discv5Client) {
    LOG.debug("Executing Discv5GetRoutingTable");
    return new Discv5GetRoutingTable(discv5Client).execute();
  }
}
