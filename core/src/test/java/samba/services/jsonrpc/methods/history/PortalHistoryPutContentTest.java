package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.TestHelper;
import samba.api.jsonrpc.done.PortalHistoryPutContent;
import samba.api.jsonrpc.results.PutContentResult;
import samba.api.libary.HistoryLibraryAPIImpl;
import samba.domain.content.ContentKey;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.util.DefaultContent;

import java.util.HashSet;
import java.util.Set;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PortalHistoryPutContentTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_PUT_CONTENT = "portal_historyPutContent";
  private PortalHistoryPutContent method;
  private HistoryNetwork historyNetwork;

  @BeforeEach
  public void before() {
    this.historyNetwork = mock(HistoryNetwork.class);
    method =
        new PortalHistoryPutContent(
            new HistoryLibraryAPIImpl(historyNetwork, mock(Discv5Client.class)));
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_PUT_CONTENT);
  }

  @Test
  public void shouldReturnStoredAndPeerCount() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();
    final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
    Set<NodeRecord> nodes = new HashSet<>();
    for (int i = 0; i < this.historyNetwork.getMaxGossipCount(); i++)
      nodes.add(TestHelper.createNodeRecord());
    when(historyNetwork.store(any(), any())).thenReturn(true);
    when(historyNetwork.getFoundNodes(any(ContentKey.class), anyInt(), any(boolean.class)))
        .thenReturn(nodes);
    doNothing().when(historyNetwork).gossip(any(), any(), any());

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            new PutContentResult(true, this.historyNetwork.getMaxGossipCount()));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnNotStoredAndPeerCount() {
    String contentKey = DefaultContent.key1.toHexString();
    String contentValue = DefaultContent.value1.toHexString();
    final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
    Set<NodeRecord> nodes = new HashSet<>();
    for (int i = 0; i < this.historyNetwork.getMaxGossipCount(); i++)
      nodes.add(TestHelper.createNodeRecord());
    when(historyNetwork.store(any(), any())).thenReturn(false);
    when(historyNetwork.getFoundNodes(any(ContentKey.class), anyInt(), any(boolean.class)))
        .thenReturn(nodes);
    doNothing().when(historyNetwork).gossip(any(), any(), any());

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(),
            new PutContentResult(false, this.historyNetwork.getMaxGossipCount()));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(JSON_RPC_VERSION, PORTAL_HISTORY_PUT_CONTENT, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @NotNull
  private JsonRpcRequestContext createRequest(String contentKey, String contentValue) {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_PUT_CONTENT,
                new Object[] {contentKey, contentValue}));
    return request;
  }
}
