package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.jsonrpc.PortalHistoryGetContent;
import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.GetContentResult;
import samba.domain.content.ContentKey;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryGetContentTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_GET_CONTENT = "portal_historyGetContent";
  private PortalHistoryGetContent method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method = new PortalHistoryGetContent(historyJsonRpc);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_GET_CONTENT);
  }

  @Test
  public void shouldReturnCorrectResultFromLocalStorage() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    final String foundData = "0x1234";
    when(historyJsonRpc.getLocalContent(any(ContentKey.class))).thenReturn(Optional.of(foundData));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(), new GetContentResult(foundData, false));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultFromNetwork() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    final String foundData = "0x1234";
    when(historyJsonRpc.getContent(any(ContentKey.class), anyInt()))
        .thenReturn(
            CompletableFuture.completedFuture(
                Optional.of(new FindContentResult(foundData, false))));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(), new GetContentResult(foundData, false));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnContentNotFoundError() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    when(historyJsonRpc.getContent(any(ContentKey.class), anyInt()))
        .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(
            request.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_GET_CONTENT, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
