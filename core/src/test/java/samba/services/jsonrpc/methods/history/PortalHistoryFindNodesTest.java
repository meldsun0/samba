package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.jsonrpc.done.PortalHistoryFindNodes;
import samba.api.HistoryAPIClient;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;

import java.util.List;
import java.util.Optional;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalHistoryFindNodesTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_FIND_NODES = "portal_historyFindNodes";
  private PortalHistoryFindNodes method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method =
        new PortalHistoryFindNodes(
                new HistoryAPIClient(historyJsonRpc));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_FIND_NODES);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_FIND_NODES,
                new Object[] {enr, new Integer[] {0}}));

    when(historyJsonRpc.findNodes(any(NodeRecord.class), any(FindNodes.class)))
        .thenReturn(
            SafeFuture.completedFuture(
                Optional.of(
                    new Nodes(
                        List.of(
                            "-I24QCThRkLZUTwzWiqrlKot9ERKue2BZwzyYO1U5V5McnbfPqIi1QTUc8OI-9YH5LBMGlFua2aGk8Y7PmY6G-nkPGkBY2GCaWSCdjSCaXCErBEABYlzZWNwMjU2azGhA8W6kHZIRlK1YMeKHGzOY5E5z5YMwzXirOxv2ty9kG4Pg3RjcIIjgoN1ZHCCIyg")))));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            List.of(
                "enr:-I24QCThRkLZUTwzWiqrlKot9ERKue2BZwzyYO1U5V5McnbfPqIi1QTUc8OI-9YH5LBMGlFua2aGk8Y7PmY6G-nkPGkBY2GCaWSCdjSCaXCErBEABYlzZWNwMjU2azGhA8W6kHZIRlK1YMeKHGzOY5E5z5YMwzXirOxv2ty9kG4Pg3RjcIIjgoN1ZHCCIyg"));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_FIND_NODES, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
