package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5DeleteEnr;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.services.discovery.Discv5Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5DeleteEnrTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_DELETE_ENR = "discv5_deleteEnr";
  private Discv5Client discv5Client;
  private Discv5DeleteEnr method;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5DeleteEnr(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_DELETE_ENR);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                DISCV5_DELETE_ENR,
                new Object[] {
                  "0xbb19e64f21d50187b61f4c68b2090db0a3283fe54021902822ff6ea0132568be"
                }));
    when(discv5Client.deleteEnr(anyString())).thenReturn(true);
    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNodIdIsNotValid() {
    final String nodeId = "nodeId";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_DELETE_ENR, new Object[] {nodeId}));

    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_DELETE_ENR, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
