package samba.services.jsonrpc.methods.discv5;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.Discv5APIClient;
import samba.api.jsonrpc.Discv5TalkReq;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.services.discovery.Discv5Client;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class Discv5TalkReqTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String DISCV5_TALK_REQ = "discv5_talkReq";
  private final String enr =
      "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";

  private Discv5Client discv5Client;
  private Discv5TalkReq method;

  @BeforeEach
  public void before() {
    this.discv5Client = mock(Discv5Client.class);
    method = new Discv5TalkReq(new Discv5APIClient(discv5Client));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(DISCV5_TALK_REQ);
  }

  @Test
  public void shouldReturnCorrectResult() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_TALK_REQ, new Object[] {enr, "0xaabbcc", "0xaa"}));
    when(discv5Client.talk(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(SafeFuture.completedFuture(Bytes.of(1)));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), Bytes.of(1).toHexString());
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParametersAreSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, DISCV5_TALK_REQ, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsParametersAreEmpty() {
    testInvalidInputValues(enr, "", "");
    testInvalidInputValues(enr, "0xaabbcc", "");
    testInvalidInputValues(enr, "", "0xaabbcc");
    testInvalidInputValues(enr, "0x", null);
    testInvalidInputValues(enr, null, "0x");
    testInvalidInputValues(enr, "0x", "0x");
  }

  private void testInvalidInputValues(String enr, String protocolId, String talkReqPayload) {
    JsonRpcResponse actual;
    JsonRpcResponse expected;
    JsonRpcRequestContext request;
    request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_TALK_REQ, new Object[] {enr, protocolId, talkReqPayload}));
    expected = new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultOfEmptyValue() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_TALK_REQ, new Object[] {enr, "0xaabbcc", "0xaa"}));
    when(discv5Client.talk(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(SafeFuture.completedFuture(Bytes.EMPTY));
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(request.getRequest().getId(), Bytes.EMPTY.toHexString());
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultWhenErrorOnInternalMethodCalled() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, DISCV5_TALK_REQ, new Object[] {enr, "0xaabbcc", "0xaa"}));
    when(discv5Client.talk(any(NodeRecord.class), any(Bytes.class), any(Bytes.class)))
        .thenReturn(SafeFuture.failedFuture(new InterruptedException()));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
