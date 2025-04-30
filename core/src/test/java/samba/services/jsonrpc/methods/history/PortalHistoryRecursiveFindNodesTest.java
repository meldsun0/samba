package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.HistoryAPIClient;
import samba.api.jsonrpc.PortalHistoryRecursiveFindNodes;
import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryRecursiveFindNodesTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_RECURSIVE_FIND_NODES = "portal_historyRecursiveFindNodes";
  private PortalHistoryRecursiveFindNodes method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method = new PortalHistoryRecursiveFindNodes(new HistoryAPIClient(historyJsonRpc));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_RECURSIVE_FIND_NODES);
  }

  @Test
  public void shouldReturnEnrList() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_RECURSIVE_FIND_NODES,
                new Object[] {
                  "0x0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"
                }));
    RecursiveFindNodesResult foundData =
        new RecursiveFindNodesResult(List.of("0x0001", "0x0002", "0x0003", "0x0004", "0x0005"));
    when(historyJsonRpc.recursiveFindNodes(any(String.class), anyInt()))
        .thenReturn(Optional.of(foundData));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), foundData);
    final JsonRpcResponse actual = method.response(request);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_RECURSIVE_FIND_NODES, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertThat(actual).isEqualTo(expected);
  }
}
