package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.HistoryAPIClient;
import samba.api.jsonrpc.PortalHistoryRoutingTableInfo;
import samba.api.jsonrpc.results.NodeInfo;
import samba.api.jsonrpc.results.RoutingTableInfoResult;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryRoutingTableInfoTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_ROUTING_TABLE_INFO = "portal_historyRoutingTableInfo";
  private final NodeRecord nodeRecord =
      NodeRecordFactory.DEFAULT.fromEnr(
          "enr:-LS4QOhHz1hd6Sg6dAtYL1XDsMN-8Quk0dmH_RhY50nVAGApdpEcK15YxNZhhDFIqNAACi8E3H1GIbtKgQsaM2TDVkyEZ1t3LGOqdCBjMjhjMzFmODViNTU4NDM0MWE0ZDQ0NTliODg2M2VjYThkOTRhY2Q2gmlkgnY0gmlwhKwRAAWJc2VjcDI1NmsxoQLv0EURHW2Rbcuk5hmsN7ZjorMOktgSBDB6n_kYOo-wc4N1ZHCCIzE");

  private HistoryAPIClient historyAPIClient;
  private Discv5APIClient discv5APIClient;
  private PortalHistoryRoutingTableInfo method;

  @BeforeEach
  public void before() {
    this.discv5APIClient = mock(Discv5APIClient.class);
    this.historyAPIClient = mock(HistoryAPIClient.class);
    method = new PortalHistoryRoutingTableInfo(this.historyAPIClient, this.discv5APIClient);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_ROUTING_TABLE_INFO);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_ROUTING_TABLE_INFO, new Object[] {}));
    final NodeInfo info = buildNodeInfo(nodeRecord);
    List<List<String>> routingTable = new ArrayList<>();
    when(historyAPIClient.getRoutingTable()).thenReturn(Optional.of(routingTable));
    when(discv5APIClient.getNodeInfo()).thenReturn(Optional.of(info));

    JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            new RoutingTableInfoResult(info.getNodeId(), routingTable));
    JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    when(discv5APIClient.getNodeInfo()).thenReturn(Optional.empty());
    expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_ROUTING_TABLE_INFO, new Object[] {}));
    final NodeInfo info = buildNodeInfo(nodeRecord);
    when(historyAPIClient.getRoutingTable()).thenReturn(Optional.empty());
    when(discv5APIClient.getNodeInfo()).thenReturn(Optional.of(info));

    JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    List<List<String>> routingTable = new ArrayList<>();
    when(historyAPIClient.getRoutingTable()).thenReturn(Optional.of(routingTable));
    when(discv5APIClient.getNodeInfo()).thenReturn(Optional.empty());

    expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    when(historyAPIClient.getRoutingTable()).thenReturn(Optional.empty());
    when(discv5APIClient.getNodeInfo()).thenReturn(Optional.empty());

    expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  private NodeInfo buildNodeInfo(NodeRecord nodeRecord) {
    return new NodeInfo(nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
  }
}
