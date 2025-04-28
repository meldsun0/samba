package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5FindNode {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5FindNode.class);

  private final Discv5Client discv5Client;

  public Discv5FindNode(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<List<String>> execute(final String enr, final Set<Integer> distances) {
    try {
      Collection<NodeRecord> nodeRecords =
          this.discv5Client
              .findNodes(NodeRecordFactory.DEFAULT.fromEnr(enr), distances.stream().toList())
              .get();
      return Optional.of(nodeRecords.stream().map(item -> item.asEnr().replace("=", "")).toList());
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing Discv5FindNodes operation");
      return Optional.empty();
    }
  }

  public static Optional<List<String>> execute(
      final Discv5Client discv5Client, final String enr, final Set<Integer> distances) {
    LOG.debug("Executing FindNodes with parameters enr:{} and contentKey {}", enr, distances);
    return new Discv5FindNode(discv5Client).execute(enr, distances);
  }
}
