package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.HistoryAPIClient;
import samba.api.jsonrpc.PortalHistoryAddEnr;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryAddEnrTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_ADD_ENR = "portal_historyAddEnr";
  private PortalHistoryAddEnr method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method = new PortalHistoryAddEnr(new HistoryAPIClient(historyJsonRpc));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_ADD_ENR);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_ADD_ENR, new Object[] {enr}));

    when(historyJsonRpc.addEnr(anyString())).thenReturn(true);

    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  //  @Test
  //  public void shouldReturnInvalidResultAsENRIsNotValid() {
  //    final String enr = "enr:-IS4QHkAX2H0A";
  //    final JsonRpcRequestContext request =
  //        new JsonRpcRequestContext(
  //            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_ADD_ENR, new Object[] {enr}));
  //
  //    doNothing().when(historyJsonRpc).addEnr(enr);
  //
  //    final JsonRpcErrorResponse expected =
  //        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
  //    final JsonRpcResponse actual = method.response(request);
  //    assertNotNull(actual);
  //    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  //  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_ADD_ENR, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
