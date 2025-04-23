package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.jsonrpc.done.PortalHistoryOffer;
import samba.api.libary.HistoryLibraryAPIImpl;
import samba.domain.messages.requests.Offer;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.util.DefaultContent;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalHistoryOfferTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_OFFER = "portal_historyOffer";
  private PortalHistoryOffer method;
  private HistoryNetwork historyJsonRpc;
  private final String enr =
      "enr:-IS4QHkAX2KwGc0IOSsAtUK9PMLPn7dMc10BWZrGaoSr74yuCulXFaA4NQ3DjAzZ8ptrKAe9lpd8eQ6lRLU4-PROxbUBgmlkgnY0gmlwhFJuaeqJc2VjcDI1NmsxoQPdVGJ30CiieHGa9seXZI2O9EFzyeed2VnGvn98pr5vSoN1ZHCCH0A";

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method =
        new PortalHistoryOffer(
            new HistoryLibraryAPIImpl(this.historyJsonRpc, mock(Discv5Client.class)));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_OFFER);
  }

  @Test
  public void shouldReturn1BitList() {
    String[] keyValue1 =
        new String[] {DefaultContent.key1.toHexString(), DefaultContent.value1.toHexString()};
    String[] keyValue2 =
        new String[] {DefaultContent.key2.toHexString(), DefaultContent.value2.toHexString()};

    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_OFFER,
                new Object[] {enr, new String[][] {keyValue1, keyValue2}}));

    when(historyJsonRpc.offer(any(NodeRecord.class), anyList(), any(Offer.class)))
        .thenReturn(
            SafeFuture.completedFuture(Optional.of(Bytes.concatenate(Bytes.of(1), Bytes.of(1)))));

    JsonRpcResponse actual = method.response(request);
    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            Bytes.concatenate(Bytes.of(1), Bytes.of(1)).toHexString());
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnErrorAsNoKeyIsProvidedResponse() {
    String[] keyValue1 = new String[] {DefaultContent.key1.toHexString(), ""};
    String[] keyValue2 =
        new String[] {DefaultContent.key2.toHexString(), DefaultContent.value2.toHexString()};

    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_OFFER,
                new Object[] {enr, new String[][] {keyValue1, keyValue2}}));

    when(historyJsonRpc.offer(any(NodeRecord.class), anyList(), any(Offer.class)))
        .thenReturn(
            SafeFuture.completedFuture(Optional.of(Bytes.concatenate(Bytes.of(1), Bytes.of(1)))));

    JsonRpcResponse actual = method.response(request);
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResponseAsNoContentIsProvidedResponse() {
    String[] keyValue1 =
        new String[] {DefaultContent.key1.toHexString(), DefaultContent.key2.toHexString()};
    String[] keyValue2 = new String[] {DefaultContent.key2.toHexString(), ""};

    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_OFFER,
                new Object[] {enr, new String[][] {keyValue1, keyValue2}}));

    when(historyJsonRpc.offer(any(NodeRecord.class), anyList(), any(Offer.class)))
        .thenReturn(
            SafeFuture.completedFuture(Optional.of(Bytes.concatenate(Bytes.of(1), Bytes.of(1)))));

    JsonRpcResponse actual = method.response(request);
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResponseWhenOfferMethodReturnsEmpty() {
    String[] keyValue1 =
        new String[] {DefaultContent.key1.toHexString(), DefaultContent.key2.toHexString()};
    String[] keyValue2 =
        new String[] {DefaultContent.key2.toHexString(), DefaultContent.value2.toHexString()};

    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_OFFER,
                new Object[] {enr, new String[][] {keyValue1, keyValue2}}));

    when(historyJsonRpc.offer(any(NodeRecord.class), anyList(), any(Offer.class)))
        .thenReturn(SafeFuture.completedFuture(Optional.empty()));

    JsonRpcResponse actual = method.response(request);
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
