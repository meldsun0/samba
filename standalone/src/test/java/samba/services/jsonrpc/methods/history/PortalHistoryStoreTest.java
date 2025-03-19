package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryStoreTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_STORE = "portal_historyStore";
  private PortalHistoryStore method;
  private HistoryNetwork historyNetwork;

  @BeforeEach
  public void before() {
    this.historyNetwork = mock(HistoryNetwork.class);
    method = new PortalHistoryStore(historyNetwork);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_STORE);
  }

  @Test
  public void shouldReturnCorrectResultSuccessfulStore() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_STORE,
                new Object[] {
                  "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c",
                  "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    when(historyNetwork.store(any(), any())).thenReturn(true);
    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultUnsuccessfulStore() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_STORE,
                new Object[] {
                  "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c",
                  "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    when(historyNetwork.store(any(), any())).thenReturn(false);
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), false);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_STORE, new Object[] {}));
    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsEmptyContentKeyIsSent() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_STORE,
                new Object[] {
                  null, "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsEmptyContentValueIsSent() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_STORE,
                new Object[] {
                  "0x00720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c", null
                }));
    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoContentKeyAndValueIsSent() {
    final JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_STORE, new Object[] {Bytes.EMPTY, Bytes.EMPTY}));
    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
