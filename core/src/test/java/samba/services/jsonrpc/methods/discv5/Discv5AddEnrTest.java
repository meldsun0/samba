package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5AddEnr;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.services.discovery.Discv5Client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5AddEnrTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_ADD_ENR = "discv5_addEnr";
  private Discv5Client discv5Client;
  private Discv5AddEnr method;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5AddEnr(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_ADD_ENR);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_ADD_ENR, new Object[] {enr}));

    when(discv5Client.addEnr(anyString())).thenReturn(true);

    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), true);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_ADD_ENR, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
