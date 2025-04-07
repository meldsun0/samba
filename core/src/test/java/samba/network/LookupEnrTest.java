package samba.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.TestHelper;
import samba.domain.messages.response.Nodes;
import samba.network.history.HistoryJsonRpcRequests;
import samba.network.history.HistoryNetwork;
import samba.services.HistoryDB;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

public class LookupEnrTest {

  private HistoryJsonRpcRequests historyNetwork;
  private NodeRecord nodeRecord;
  private RoutingTable routingTable;
  private Discv5Client discv5Client;

  @BeforeEach
  public void setUp() throws IllegalAccessException {
    this.discv5Client = mock(Discv5Client.class);
    HistoryDB historyDB = mock(HistoryDB.class);
    when(historyDB.isAvailable()).thenReturn(true);
    this.historyNetwork = new HistoryNetwork(discv5Client, historyDB, mock(UTPManager.class));
    this.nodeRecord = TestHelper.createNodeRecord();
    this.routingTable = this.mockRoutingTable();
  }

  @Test
  public void testLookupEnrIsSameAsHomeNodeRecord() {
    when(this.discv5Client.getHomeNodeRecord()).thenReturn(this.nodeRecord);
    Optional<String> enr =
        this.historyNetwork.lookupEnr(
            UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString()));
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  @Test
  public void testLookupEnrIsNotFoundOnRoutingTableSoShouldReturnFromDiscv5RoutingTable() {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());
    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.discv5Client.lookupEnr(nodeId)).thenReturn(Optional.of(this.nodeRecord.asEnr()));

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  @Test
  public void testLookupEnrFoundOnRoutingTableSoFindNodeIsCalledAndReturnWithHigherSeq()
      throws IllegalAccessException, NoSuchFieldException {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());
    UInt64 originalSeq = this.nodeRecord.getSeq();
    NodeRecord updatedNodeRecord = this.incrementSeq(this.nodeRecord, originalSeq.add(1));
    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.routingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(this.nodeRecord));
    whenFindNodes(List.of(updatedNodeRecord.asBase64()));

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(updatedNodeRecord.asEnr(), enr.get());
    assertEquals(updatedNodeRecord.getSeq(), originalSeq.add(1));
  }

  @Test
  public void
      testLookupEnrFoundOnRoutingTableSoFindNodeIsCalledAndReturnWithSameSeqSoOriginalNodeRecordMustBeAnswered()
          throws IllegalAccessException, NoSuchFieldException {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());

    NodeRecord updatedNodeRecord = this.incrementSeq(this.nodeRecord, this.nodeRecord.getSeq());
    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.routingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(this.nodeRecord));
    whenFindNodes(List.of(updatedNodeRecord.asBase64()));

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  @Test
  public void testLookupEnrFoundOnRoutingTableSoFindNodeIsCalledButReturnEmptyListSoDicv5IsCalled()
      throws IllegalAccessException, NoSuchFieldException {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());

    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.routingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(this.nodeRecord));
    whenFindNodes(List.of());
    when(this.discv5Client.lookupEnr(nodeId)).thenReturn(Optional.of(this.nodeRecord.asEnr()));

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  @Test
  public void
      testLookupEnrFoundOnRoutingTableSoFindNodeIsCalledButReturnEmptyListSoDicv5IsCalledWithError()
          throws IllegalAccessException, NoSuchFieldException {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());

    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.routingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(this.nodeRecord));
    whenFindNodes(List.of());
    when(this.discv5Client.lookupEnr(nodeId)).thenReturn(Optional.empty());

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  @Test
  public void testLookupEnrFoundOnRoutingTableWhenFindNodesThrowAnException()
      throws IllegalAccessException, NoSuchFieldException {
    UInt256 nodeId = UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString());

    when(this.discv5Client.getHomeNodeRecord()).thenReturn(TestHelper.createNodeRecord());
    when(this.routingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(this.nodeRecord));
    when(this.discv5Client.sendDisv5Message(
            any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(invocation -> CompletableFuture.failedFuture(new RuntimeException()));

    when(this.discv5Client.lookupEnr(nodeId)).thenReturn(Optional.of(this.nodeRecord.asEnr()));

    Optional<String> enr = this.historyNetwork.lookupEnr(nodeId);
    assertNotNull(enr);
    assertTrue(enr.isPresent());
    assertEquals(this.nodeRecord.asEnr(), enr.get());
  }

  private void whenFindNodes(List<String> enrs) {
    when(this.discv5Client.sendDisv5Message(
            any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenAnswer(
            invocation -> CompletableFuture.completedFuture((new Nodes(enrs)).getSszBytes()));
  }

  private RoutingTable mockRoutingTable() throws IllegalAccessException {
    RoutingTable mockedRoutingTable = mock(RoutingTable.class);
    Field field =
        ReflectionUtils.findFields(
                HistoryNetwork.class,
                f -> f.getName().equals("routingTable"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);
    field.setAccessible(true);
    field.set(historyNetwork, mockedRoutingTable);
    return mockedRoutingTable;
  }

  private NodeRecord incrementSeq(NodeRecord nodeRecord, UInt64 newSeq) {
    List<EnrField> enrFields = new ArrayList<>();
    nodeRecord.forEachField(
        (fieldName, fieldValue) -> enrFields.add(new EnrField(fieldName, fieldValue)));
    return NodeRecordFactory.DEFAULT.createFromValues(newSeq, enrFields);
  }
}
