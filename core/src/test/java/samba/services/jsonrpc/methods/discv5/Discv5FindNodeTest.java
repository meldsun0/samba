package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5FindNode;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.services.discovery.Discv5Client;

import java.util.List;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class Discv5FindNodeTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_FIND_NODE = "discv5_findNode";
  private final String enr =
      "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
  ;
  private Discv5Client discv5Client;
  private Discv5FindNode method;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5FindNode(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_FIND_NODE);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_FIND_NODE, new Object[] {enr, new Integer[] {0}}));
    NodeRecord nodeRecord =
        NodeRecordFactory.DEFAULT.fromEnr(
            "-I24QCThRkLZUTwzWiqrlKot9ERKue2BZwzyYO1U5V5McnbfPqIi1QTUc8OI-9YH5LBMGlFua2aGk8Y7PmY6G-nkPGkBY2GCaWSCdjSCaXCErBEABYlzZWNwMjU2azGhA8W6kHZIRlK1YMeKHGzOY5E5z5YMwzXirOxv2ty9kG4Pg3RjcIIjgoN1ZHCCIyg");
    when(discv5Client.findNodes(any(NodeRecord.class), any(List.class)))
        .thenReturn(SafeFuture.completedFuture(List.of(nodeRecord)));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), List.of(nodeRecord.asEnr()));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParametersAreSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_FIND_NODE, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultOfEmptyList() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_FIND_NODE, new Object[] {enr, new Integer[] {0}}));
    when(discv5Client.findNodes(any(NodeRecord.class), any(List.class)))
        .thenReturn(SafeFuture.completedFuture(List.of()));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), List.of());
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultWhenErrorOnInternalMethodCalled() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_FIND_NODE, new Object[] {enr, new Integer[] {0}}));
    when(discv5Client.findNodes(any(NodeRecord.class), any(List.class)))
        .thenReturn(SafeFuture.failedFuture(new InterruptedException()));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
