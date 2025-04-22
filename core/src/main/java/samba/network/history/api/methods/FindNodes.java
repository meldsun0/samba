package samba.network.history.api.methods;

import samba.domain.messages.response.Nodes;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindNodes {

  private static final Logger LOG = LoggerFactory.getLogger(FindNodes.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public FindNodes(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private List<String> execute(final String enr, final Set<Integer> distances) {
    try {
      Optional<Nodes> result =
          this.historyNetworkInternalAPI
              .findNodes(
                  NodeRecordFactory.DEFAULT.fromEnr(enr),
                  new samba.domain.messages.requests.FindNodes(distances))
              .get();
      if (result.isPresent()) {
        return result.get().getEnrsWithENRPerItem();
      }
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing FindNodes operation");
    }
    return List.of();
  }

  public static List<String> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI,
      final String enr,
      final Set<Integer> distances) {
    LOG.debug("Executing FindNodes with parameters enr:{} and contentKey {}", enr, distances);
    return new FindNodes(historyNetworkInternalAPI).execute(enr, distances);
  }
}
