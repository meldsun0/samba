package samba.network.history;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static samba.TestHelper.createNodeRecord;

import samba.network.RoutingTable;
import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPService;
import samba.storage.HistoryDB;

import java.lang.reflect.Field;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

public class GetEnrTest {

  private HistoryDB historyDB;
  private HistoryJsonRpcRequests historyNetwork;
  private Discv5Client discv5Client;
  private final String nodeId =
      "0xbb19e64f21d50187b61f4c68b2090db0a3283fe54021902822ff6ea0132568be";

  @BeforeEach
  public void setUp() {
    this.discv5Client = mock(Discv5Client.class);
    this.historyDB = mock(HistoryDB.class);
    this.historyNetwork = new HistoryNetwork(discv5Client, historyDB, mock(UTPService.class));
    when(discv5Client.getHomeNodeRecord()).thenReturn(createNodeRecord());
  }

  @Test
  public void testGetEnrHomeNodeRecordIsNull() {
    when(discv5Client.getHomeNodeRecord()).thenReturn(null);
    Optional<String> result = historyNetwork.getEnr(nodeId);
    assertFalse(result.isPresent(), "Expected Optional.empty() when homeNodeRecord is null");
  }

  @Test
  public void testGetEnrHomeNodeRecordMatchesNodeId() {
    Bytes nodeIdInBytes = Bytes.fromHexString(nodeId);

    NodeRecord homeNodeRecord = mock(NodeRecord.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
    when(homeNodeRecord.getNodeId()).thenReturn(nodeIdInBytes);
    when(homeNodeRecord.asEnr()).thenReturn("enr:test");

    Optional<String> result = historyNetwork.getEnr(nodeId);

    assertTrue(result.isPresent(), "Expected ENR value when homeNodeRecord nodeId matches");
    assertEquals("enr:test", result.get(), "Expected ENR string to be 'enr:test'");
  }

  @Test
  public void testGetEnrNodeIdDoesNotMatchWithLocalNnodeAndRoutingTableHasNode() throws Exception {
    NodeRecord homeNodeRecord = mock(NodeRecord.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
    when(homeNodeRecord.getNodeId())
        .thenReturn(
            Bytes.fromHexString(
                "0x0e208c8ab86ad547a60bceeebe64e6f6f2c6fcf37e24943f345731b0c159b5dd"));
    when(homeNodeRecord.asEnr()).thenReturn("enr");

    RoutingTable mockedRoutingTable = mock(RoutingTable.class);
    Field field =
        ReflectionUtils.findFields(
                HistoryNetwork.class,
                f -> f.getName().equals("routingTable"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);
    field.setAccessible(true);
    field.set(historyNetwork, mockedRoutingTable);

    when(mockedRoutingTable.findNode(any(Bytes.class))).thenReturn(Optional.of(homeNodeRecord));

    Optional<String> result = historyNetwork.getEnr(nodeId);

    assertTrue(result.isPresent(), "Expected ENR from routing table");
    assertEquals("enr", result.get(), "Expected ENR to be from routing table");
  }

  @Test
  public void testGetEnrNodeIdDoesNotMatchAndRoutingTableDoesNotHaveNode() throws Exception {
    NodeRecord homeNodeRecord = mock(NodeRecord.class);
    when(discv5Client.getHomeNodeRecord()).thenReturn(homeNodeRecord);
    when(homeNodeRecord.getNodeId())
        .thenReturn(
            Bytes.fromHexString(
                "0x0e208c8ab86ad547a60bceeebe64e6f6f2c6fcf37e24943f345731b0c159b5dd"));
    when(homeNodeRecord.asEnr()).thenReturn("enr");

    RoutingTable mockedRoutingTable = mock(RoutingTable.class);
    Field field =
        ReflectionUtils.findFields(
                HistoryNetwork.class,
                f -> f.getName().equals("routingTable"),
                ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
            .get(0);
    field.setAccessible(true);
    field.set(historyNetwork, mockedRoutingTable);

    when(mockedRoutingTable.findNode(any(Bytes.class))).thenReturn(Optional.empty());

    Optional<String> result = historyNetwork.getEnr(nodeId);

    assertFalse(
        result.isPresent(), "Expected Optional.empty() when node is not found in routing table");
  }
}
