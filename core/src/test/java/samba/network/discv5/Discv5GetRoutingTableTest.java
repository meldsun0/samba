package samba.network.discv5;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.TestHelper;
import samba.network.history.api.methods.Discv5GetRoutingTable;
import samba.services.discovery.Discv5Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5GetRoutingTableTest {

  private Discv5Client discv5Client;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
  }

  @Test
  void testExecuteWithEmptyRoutingTable() {
    when(discv5Client.getRoutingTable()).thenReturn(Collections.emptyList());
    Optional<List<List<String>>> result = Discv5GetRoutingTable.execute(this.discv5Client);
    assertTrue(result.isPresent());
    assertTrue(result.get().isEmpty());
  }

  @Test
  void testExecuteWithSingleShortList() {
    List<List<NodeRecord>> routingTable = new ArrayList<>();
    routingTable.add(Arrays.asList(TestHelper.createNodeRecord(), TestHelper.createNodeRecord()));
    routingTable.add(Arrays.asList(TestHelper.createNodeRecord(), TestHelper.createNodeRecord()));
    routingTable.add(Arrays.asList(TestHelper.createNodeRecord()));
    when(discv5Client.getRoutingTable()).thenReturn(routingTable);

    Optional<List<List<String>>> result = Discv5GetRoutingTable.execute(this.discv5Client);

    assertTrue(result.isPresent());
    List<List<String>> outerList = result.get();
    assertEquals(3, outerList.size());
    assertEquals(2, outerList.get(0).size());
    assertEquals(2, outerList.get(1).size());
    assertEquals(1, outerList.get(2).size());
  }

  @Test
  void testExecuteEnforcesLimit() {
    List<NodeRecord> largeBucket =
        IntStream.range(0, 20).mapToObj(i -> TestHelper.createNodeRecord()).toList();

    when(discv5Client.getRoutingTable()).thenReturn(List.of(largeBucket));

    Optional<List<List<String>>> result = Discv5GetRoutingTable.execute(this.discv5Client);

    assertTrue(result.isPresent());
    List<String> inner = result.get().get(0);
    assertEquals(Discv5GetRoutingTable.MAX_ROUTING_TABLE_SIZE, inner.size());
  }

  @Test
  void testExecuteOrderfromLeastRecentlySeenToMostRecentlySeen() {
    final NodeRecord nodeRecord =
        NodeRecordFactory.DEFAULT.fromEnr(
            "enr:-LS4QOhHz1hd6Sg6dAtYL1XDsMN-8Quk0dmH_RhY50nVAGApdpEcK15YxNZhhDFIqNAACi8E3H1GIbtKgQsaM2TDVkyEZ1t3LGOqdCBjMjhjMzFmODViNTU4NDM0MWE0ZDQ0NTliODg2M2VjYThkOTRhY2Q2gmlkgnY0gmlwhKwRAAWJc2VjcDI1NmsxoQLv0EURHW2Rbcuk5hmsN7ZjorMOktgSBDB6n_kYOo-wc4N1ZHCCIzE");
    List<List<NodeRecord>> routingTable = new ArrayList<>();
    routingTable.add(
        Arrays.asList(TestHelper.createNodeRecord(), TestHelper.createNodeRecord(), nodeRecord));
    routingTable.add(
        Arrays.asList(nodeRecord, TestHelper.createNodeRecord(), TestHelper.createNodeRecord()));
    routingTable.add(Arrays.asList(TestHelper.createNodeRecord()));

    when(discv5Client.getRoutingTable()).thenReturn(routingTable);

    Optional<List<List<String>>> result = Discv5GetRoutingTable.execute(this.discv5Client);

    assertTrue(result.isPresent());
    List<List<String>> outerList = result.get();
    assertEquals(3, outerList.size());
    assertEquals(3, outerList.get(0).size());
    assertEquals(3, outerList.get(1).size());
    assertEquals(1, outerList.get(2).size());
    // check reversed order
    assertEquals(nodeRecord.getNodeId().toHexString(), outerList.get(0).get(0));
    assertEquals(nodeRecord.getNodeId().toHexString(), outerList.get(1).get(2));
  }
}
