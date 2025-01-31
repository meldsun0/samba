package samba.services.jsonrpc.methods;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import samba.domain.messages.requests.FindContent;
import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryNetwork;
import samba.services.jsonrpc.methods.history.PortalHistoryFindContent;
import samba.services.jsonrpc.methods.history.PortalHistoryFindNodes;
import samba.services.jsonrpc.methods.results.FindContentResult;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static samba.jsonrpc.config.RpcMethod.PORTAL_HISTORY_FIND_CONTENT;

public class PortalHistoryFindContentTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_FIND_CONTENT = "portal_historyFindContent";
  private PortalHistoryFindContent method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method = new PortalHistoryFindContent(historyJsonRpc);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_FIND_CONTENT);
  }

  @Test
  public void shouldReturnCorrectResultIfContentReturnsisAnEmptyENRList() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_FIND_CONTENT,
                new Object[] {enr, "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"}));

    when(historyJsonRpc.findContent(any(NodeRecord.class), any(FindContent.class)))
        .thenReturn(
            SafeFuture.completedFuture(
                Optional.of(
                    new FindContentResult(List.of()))));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
                new FindContentResult(List.of()));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request = new JsonRpcRequestContext(new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_FIND_CONTENT, new Object[] {}));
    final JsonRpcResponse expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
