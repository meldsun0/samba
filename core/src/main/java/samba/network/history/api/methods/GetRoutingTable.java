package samba.network.history.api.methods;

import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetRoutingTable {

  private static final Logger LOG = LoggerFactory.getLogger(GetRoutingTable.class);
  public static final int MAX_ROUTING_TABLE_SIZE = 16;

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetRoutingTable(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<List<List<String>>> execute() {
    List<List<NodeRecord>> routingTable = historyNetworkInternalAPI.getRoutingTable();
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
                  Collections.reverse(reversed); // ordered from least-recently to most-recently
                  return reversed;
                })
            .collect(Collectors.toList()));
  }

  public static Optional<List<List<String>>> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    LOG.debug("Executing GetRoutingTable");
    return new GetRoutingTable(historyNetworkInternalAPI).execute();
  }
}
