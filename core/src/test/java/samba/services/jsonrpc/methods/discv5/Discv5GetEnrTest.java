package samba.services.jsonrpc.methods.discv5;

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
import samba.services.discovery.Discv5Client;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Discv5GetEnrTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_GET_ENR = "discv5_getEnr";
  private Discv5Client discv5Client;
  private Discv5GetEnr method;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5GetEnr(discv5Client);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_GET_ENR);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final String enr =
        "enr:-LS4QOhHz1hd6Sg6dAtYL1XDsMN-8Quk0dmH_RhY50nVAGApdpEcK15YxNZhhDFIqNAACi8E3H1GIbtKgQsaM2TDVkyEZ1t3LGOqdCBjMjhjMzFmODViNTU4NDM0MWE0ZDQ0NTliODg2M2VjYThkOTRhY2Q2gmlkgnY0gmlwhKwRAAWJc2VjcDI1NmsxoQLv0EURHW2Rbcuk5hmsN7ZjorMOktgSBDB6n_kYOo-wc4N1ZHCCIzE";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                DISCV5_GET_ENR,
                new Object[] {
                  "0xbb19e64f21d50187b61f4c68b2090db0a3283fe54021902822ff6ea0132568be"
                }));
    when(discv5Client.lookupEnr(any(UInt256.class))).thenReturn(Optional.of(enr));
    final JsonRpcResponse expected = new JsonRpcSuccessResponse(request.getRequest().getId(), enr);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResult() {
    final String nodeId = "0xbb19e64f21283fe54021902822ff6ea0132568be";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_GET_ENR, new Object[] {nodeId}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_GET_ENR, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoLookUpENRIsEmpty() {
    when(discv5Client.lookupEnr(any(UInt256.class))).thenReturn(Optional.empty());
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_GET_ENR, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
