package samba.services.jsonrpc.methods;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryJsonRpcRequests;
import samba.services.jsonrpc.methods.history.PortalHistoryStore;
import samba.util.DefaultContent;

import org.apache.tuweni.bytes.Bytes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryStoreTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_STORE = "portal_historyStore";
  private HistoryJsonRpcRequests historyJsonRpcRequests;
  private PortalHistoryStore method;

  @BeforeEach
  public void before() {
    this.historyJsonRpcRequests = mock(HistoryJsonRpcRequests.class);
    method = new PortalHistoryStore(this.historyJsonRpcRequests);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_STORE);
  }

  @Test
  public void shouldReturnCorrectResult() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();

    final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
    when(historyJsonRpcRequests.store(any(Bytes.class), any(Bytes.class))).thenReturn(true);

    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyJsonRpcRequests, times(1)).store(any(Bytes.class), any(Bytes.class));
  }

  @Test
  public void shouldReturnFalseReponseResult() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();

    final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
    when(historyJsonRpcRequests.store(any(Bytes.class), any(Bytes.class))).thenReturn(false);

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), false);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyJsonRpcRequests, times(1)).store(any(Bytes.class), any(Bytes.class));
  }

  @Test
  public void shouldReturnInvalidRequestAsInputsAreEmptyResult() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();

    JsonRpcRequestContext request = createRequest("", contentValue);

    verify(this.historyJsonRpcRequests, never()).store(any(Bytes.class), any(Bytes.class));

    JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    request = createRequest(contentKey, "");
    actual = method.response(request);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    request = createRequest("", "");
    actual = method.response(request);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  //  @Test
  //  public void shouldReturnErrorIfContentKeyIsEmptyResult() {
  //    final JsonRpcRequestContext request = createRequest("");
  //    final JsonRpcResponse expected =
  //        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
  //    final JsonRpcResponse actual = method.response(request);
  //    assertNotNull(actual);
  //    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  //    verify(this.historyJsonRpcRequests, never()).getLocalContent(any(ContentKey.class));
  //  }

  //  @Test
  //  public void shouldReturnContentNotFoundResultIfContentIsNotFound() {
  //    final JsonRpcRequestContext request = createRequest(DefaultContent.key1.toHexString());
  //    when(historyJsonRpcRequests.getLocalContent(any(ContentKey.class)))
  //        .thenReturn(Optional.empty());
  //
  //    final JsonRpcResponse expected =
  //        new JsonRpcErrorResponse(
  //            request.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
  //    final JsonRpcResponse actual = method.response(request);
  //    assertNotNull(actual);
  //    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  //    verify(this.historyJsonRpcRequests, times(1)).getLocalContent(any(ContentKey.class));
  //  }

  @NotNull
  private JsonRpcRequestContext createRequest(String contentKey, String contentValue) {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_STORE, new Object[] {contentKey, contentValue}));
    return request;
  }
}
