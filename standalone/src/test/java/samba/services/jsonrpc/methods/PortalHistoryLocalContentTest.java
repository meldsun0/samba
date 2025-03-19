package samba.services.jsonrpc.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import samba.domain.content.ContentKey;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryJsonRpcRequests;
import samba.services.jsonrpc.methods.history.PortalHistoryLocalContent;
import samba.util.DefaultContent;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryLocalContentTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_LOCAL_CONTENT = "portal_historyLocalContent";
  private HistoryJsonRpcRequests historyJsonRpcRequests;
  private PortalHistoryLocalContent method;

  @BeforeEach
  public void before() {
    this.historyJsonRpcRequests = mock(HistoryJsonRpcRequests.class);
    method = new PortalHistoryLocalContent(this.historyJsonRpcRequests);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_LOCAL_CONTENT);
  }

  @Test
  public void shouldReturnCorrectResult() {
    String content = DefaultContent.value1.toHexString();

    final JsonRpcRequestContext request = createRequest(DefaultContent.key1.toHexString());
    when(historyJsonRpcRequests.getLocalContent(any(ContentKey.class)))
        .thenReturn(Optional.of(content));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), content);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyJsonRpcRequests, times(1)).getLocalContent(any(ContentKey.class));
  }

  @Test
  public void shouldReturnErrorIfContentKeyIsEmptyResult() {
    final JsonRpcRequestContext request = createRequest("");
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyJsonRpcRequests, never()).getLocalContent(any(ContentKey.class));
  }

  @Test
  public void shouldReturnContentNotFoundResultIfContentIsNotFound() {
    final JsonRpcRequestContext request = createRequest(DefaultContent.key1.toHexString());
    when(historyJsonRpcRequests.getLocalContent(any(ContentKey.class)))
        .thenReturn(Optional.empty());

    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(
            request.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyJsonRpcRequests, times(1)).getLocalContent(any(ContentKey.class));
  }

  @NotNull
  private JsonRpcRequestContext createRequest(String contentKey) {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_LOCAL_CONTENT, new Object[] {contentKey}));
    return request;
  }
}
