package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.jsonrpc.PortalHistoryPing;
import samba.api.jsonrpc.results.PingResult;
import samba.api.jsonrpc.schemas.ClientInfoAndCapabilitiesJson;
import samba.domain.messages.extensions.standard.ClientInfoAndCapabilities;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.tuweni.units.bigints.UInt256;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalHistoryPingTest {
  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_PING = "portal_historyPing";
  private PortalHistoryPing method;
  private Discv5Client discv5Client;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    this.discv5Client = mock(Discv5Client.class);
    method = new PortalHistoryPing(this.historyJsonRpc, this.discv5Client);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_PING);
  }

  @Test
  public void shouldReturnCorrectResultWhenNodeRecordIsAlreadyConnected()
      throws JsonProcessingException {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PING, new Object[] {enr}));
    final ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "clientInfo", UInt256.ONE, List.of(UInt16.ZERO, UInt16.MAX_VALUE));
    final Pong pong =
        new Pong(UInt64.valueOf(1), UInt16.ZERO, clientInfoAndCapabilities.getSszBytes());

    when(historyJsonRpc.isNodeConnected(any(NodeRecord.class))).thenReturn(true);
    when(historyJsonRpc.ping(any(NodeRecord.class)))
        .thenReturn(SafeFuture.of(() -> Optional.of(pong)));

    ClientInfoAndCapabilitiesJson clientInfoAndCapabilitiesJson =
        new ClientInfoAndCapabilitiesJson(
            clientInfoAndCapabilities.getClientInfo(),
            clientInfoAndCapabilities.getDataRadius(),
            clientInfoAndCapabilities.getCapabilities());
    Object extensionJson = clientInfoAndCapabilitiesJson;
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            new PingResult(
                pong.getEnrSeq().bigIntegerValue(), pong.getPayloadType(), extensionJson));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultWhenNodeRecordIsNotConnected()
      throws JsonProcessingException {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PING, new Object[] {enr}));
    final ClientInfoAndCapabilities clientInfoAndCapabilities =
        new ClientInfoAndCapabilities(
            "clientInfo", UInt256.ONE, List.of(UInt16.ZERO, UInt16.MAX_VALUE));
    final Pong pong =
        new Pong(UInt64.valueOf(1), UInt16.ZERO, clientInfoAndCapabilities.getSszBytes());

    when(historyJsonRpc.isNodeConnected(any(NodeRecord.class))).thenReturn(false);
    when(historyJsonRpc.ping(any(NodeRecord.class)))
        .thenReturn(SafeFuture.of(() -> Optional.of(pong)));
    when(discv5Client.ping(any(NodeRecord.class)))
        .thenReturn(Mockito.mock(CompletableFuture.class));

    ClientInfoAndCapabilitiesJson clientInfoAndCapabilitiesJson =
        new ClientInfoAndCapabilitiesJson(
            clientInfoAndCapabilities.getClientInfo(),
            clientInfoAndCapabilities.getDataRadius(),
            clientInfoAndCapabilities.getCapabilities());
    Object extensionJson = clientInfoAndCapabilitiesJson;
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            new PingResult(
                pong.getEnrSeq().bigIntegerValue(), pong.getPayloadType(), extensionJson));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidRequestResultWhenPingIsEmpty() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PING, new Object[] {enr}));
    when(historyJsonRpc.isNodeConnected(any(NodeRecord.class))).thenReturn(true);
    when(historyJsonRpc.ping(any(NodeRecord.class)))
        .thenReturn(SafeFuture.of(() -> Optional.empty()));

    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidRequestResultWhenDiscv5PingReturnsAnException() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PING, new Object[] {enr}));
    when(historyJsonRpc.isNodeConnected(any(NodeRecord.class))).thenReturn(false);

    CompletableFuture<Void> future = new CompletableFuture<>();
    future.completeExceptionally(new InterruptedException("Task failed"));

    when(discv5Client.ping(any(NodeRecord.class))).thenReturn(future);

    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidRequestResultWhenPingReturnsAnException() {
    final String enr =
        "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PING, new Object[] {enr}));
    when(historyJsonRpc.isNodeConnected(any(NodeRecord.class))).thenReturn(true);

    SafeFuture<Optional<Pong>> future = new SafeFuture<>();
    future.completeExceptionally(new InterruptedException("Task failed"));

    when(historyJsonRpc.ping(any(NodeRecord.class))).thenReturn(future);

    final JsonRpcErrorResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
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
  //
  //  @Test
  //  public void shouldReturnInvalidResultAsNoParameterisSent() {
  //    final JsonRpcRequestContext request =
  //        new JsonRpcRequestContext(
  //            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_ADD_ENR, new Object[] {}));
  //    final JsonRpcResponse expected =
  //        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
  //    final JsonRpcResponse actual = method.response(request);
  //    assertNotNull(actual);
  //    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  //  }
}
