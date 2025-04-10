package samba.network.history;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.TestHelper;
import samba.domain.dht.LivenessChecker;
import samba.network.history.routingtable.HistoryRoutingTable;
import samba.network.history.routingtable.RoutingTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class HistoryRoutingTableTest {

  private RoutingTable routingTable;
  private List<NodeRecord> nodes;
  private LivenessChecker livenessChecker;
  private NodeRecord homeNode;

  @BeforeEach
  public void setUp() {
    this.livenessChecker = Mockito.mock(LivenessChecker.class);
    this.homeNode = TestHelper.createNodeRecord();
    this.routingTable = new HistoryRoutingTable(this.homeNode, this.livenessChecker);
    this.nodes = new ArrayList<>();
    for (int i = 1; i < 15; i++) {
      NodeRecord node = TestHelper.createNodeRecord();
      this.nodes.add(node);
      this.routingTable.addOrUpdateNode(node);
      this.routingTable.updateRadius(node.getNodeId(), UInt256.valueOf(i));
    }
  }

  @Test
  public void testFindClosestNodeToContentKey() {
    NodeRecord closestNode = this.nodes.get(0);
    NodeRecord node = this.routingTable.findClosestNodeToContentKey(closestNode.getNodeId()).get();
    assertEquals(closestNode, node);
  }

  @Test
  public void testFindClosestNodeToContentKeyWithEmptyNodes() {
    Set<NodeRecord> foundNode =
        this.routingTable.findClosestNodesToContentKey(Bytes.fromHexString("0x1234"), 1, false);
    assertEquals(1, foundNode.size());
  }

  @Test
  public void testFindClosestNodesToContentKeyInRadius() {
    RoutingTable exaggeratedTable = new HistoryRoutingTable(this.homeNode, this.livenessChecker);
    exaggeratedTable.addOrUpdateNode(this.nodes.get(0));
    exaggeratedTable.updateRadius(this.nodes.get(0).getNodeId(), UInt256.MAX_VALUE);
    exaggeratedTable.addOrUpdateNode(this.nodes.get(2));
    exaggeratedTable.updateRadius(this.nodes.get(2).getNodeId(), UInt256.MAX_VALUE);
    exaggeratedTable.addOrUpdateNode(this.nodes.get(1));
    exaggeratedTable.updateRadius(this.nodes.get(1).getNodeId(), UInt256.valueOf(1));
    Set<NodeRecord> foundNodes =
        exaggeratedTable.findClosestNodesToContentKey(Bytes.fromHexString("0x1234"), 10, true);
    assertEquals(2, foundNodes.size());
    assertEquals(Set.of(this.nodes.get(0), this.nodes.get(2)), foundNodes);
  }
}
