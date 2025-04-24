package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.TestHelper;
import samba.api.jsonrpc.done.PortalHistoryLookupEnr;
import samba.api.HistoryAPIClient;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryLookupEnrTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_LOOKUP_ENR = "portal_historyLookupEnr";
  private PortalHistoryLookupEnr method;
  private HistoryNetwork historyJsonRpc;
  private NodeRecord nodeRecord;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    this.method =
        new PortalHistoryLookupEnr(
            new HistoryAPIClient(historyJsonRpc));
    this.nodeRecord = TestHelper.createNodeRecord();
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_LOOKUP_ENR);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        buildJsonRpcRequest(this.nodeRecord.getNodeId().toHexString());

    when(historyJsonRpc.lookupEnr(UInt256.fromHexString(this.nodeRecord.getNodeId().toHexString())))
        .thenReturn(Optional.of(this.nodeRecord.asEnr()));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), this.nodeRecord.asEnr());
    final JsonRpcResponse actual = method.response(request);

    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNodIdIsNotValid() {
    final String nodeId = "nodeId";
    final JsonRpcRequestContext request = buildJsonRpcRequest(nodeId);
    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParametersAreSent() {
    final JsonRpcRequestContext request = buildJsonRpcRequest("");
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  private JsonRpcRequestContext buildJsonRpcRequest(String nodeId) {
    return new JsonRpcRequestContext(
        new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_LOOKUP_ENR, new Object[] {nodeId}));
  }
}
