package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import samba.api.jsonrpc.done.PortalHistoryStore;
import samba.api.libary.HistoryLibraryAPIImpl;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;
import samba.util.DefaultContent;

import org.apache.tuweni.bytes.Bytes;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryStoreTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_STORE = "portal_historyStore";
  private HistoryNetworkInternalAPI historyNetworkInternalAPI;
  private PortalHistoryStore method;

  @BeforeEach
  public void before() {
    this.historyNetworkInternalAPI = mock(HistoryNetworkInternalAPI.class);
    method = new PortalHistoryStore(new HistoryLibraryAPIImpl(this.historyNetworkInternalAPI));
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
    when(historyNetworkInternalAPI.store(any(Bytes.class), any(Bytes.class))).thenReturn(true);

    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyNetworkInternalAPI, times(1)).store(any(Bytes.class), any(Bytes.class));
  }

  @Test
  public void shouldReturnFalseReponseResult() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();

    final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
    when(historyNetworkInternalAPI.store(any(Bytes.class), any(Bytes.class))).thenReturn(false);

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), false);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    verify(this.historyNetworkInternalAPI, times(1)).store(any(Bytes.class), any(Bytes.class));
  }

  @Test
  public void shouldReturnInvalidRequestAsInputsAreEmptyResult() {
    String contentValue = DefaultContent.value1.toHexString();

    JsonRpcRequestContext request = createRequest("", contentValue);

    verify(this.historyNetworkInternalAPI, never()).store(any(Bytes.class), any(Bytes.class));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), false);
    JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);

    request = createRequest("", "");
    actual = method.response(request);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnOkIfContentKeyIsEmpty() {
    String contentKey = DefaultContent.key1.toHexString();
    JsonRpcRequestContext request = createRequest(contentKey, "");
    when(historyNetworkInternalAPI.store(any(Bytes.class), any(Bytes.class))).thenReturn(true);

    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    JsonRpcResponse actual = method.response(request);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @NotNull
  private JsonRpcRequestContext createRequest(String contentKey, String contentValue) {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_STORE, new Object[] {contentKey, contentValue}));
    return request;
  }
}
