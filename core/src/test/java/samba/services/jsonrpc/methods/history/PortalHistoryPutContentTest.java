package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import samba.TestHelper;
import samba.domain.content.ContentKey;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.PortalGossip;
import samba.network.history.HistoryNetwork;
import samba.services.jsonrpc.methods.results.PutContentResult;
import samba.util.DefaultContent;

import java.util.HashSet;
import java.util.Set;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class PortalHistoryPutContentTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_PUT_CONTENT = "portal_historyPutContent";
  private PortalHistoryPutContent method;
  private HistoryNetwork historyJsonRpc;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    method = new PortalHistoryPutContent(historyJsonRpc);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_PUT_CONTENT);
  }

  @Test
  public void shouldReturnStoredAndPeerCount() {
    try (MockedStatic<PortalGossip> mocked = mockStatic(PortalGossip.class)) {
      String contentKey = DefaultContent.key1.toHexString();
      String contentValue = DefaultContent.value1.toHexString();
      final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
      Set<NodeRecord> nodes = new HashSet<>();
      for (int i = 0; i < PortalGossip.MAX_GOSSIP_COUNT; i++)
        nodes.add(TestHelper.createNodeRecord());
      when(historyJsonRpc.store(any(), any())).thenReturn(true);
      when(historyJsonRpc.getFoundNodes(any(ContentKey.class), anyInt(), any(boolean.class)))
          .thenReturn(nodes);
      mocked.when(() -> PortalGossip.gossip(any(), any(), any(), any())).thenReturn(null);

      final JsonRpcResponse expected =
          new JsonRpcSuccessResponse(
              request.getRequest().getId(),
              new PutContentResult(true, PortalGossip.MAX_GOSSIP_COUNT));
      final JsonRpcResponse actual = method.response(request);
      assertNotNull(actual);
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void shouldReturnNotStoredAndPeerCount() {
    try (MockedStatic<PortalGossip> mocked = mockStatic(PortalGossip.class)) {
      String contentKey = DefaultContent.key1.toHexString();
      String contentValue = DefaultContent.value1.toHexString();
      final JsonRpcRequestContext request = createRequest(contentKey, contentValue);
      Set<NodeRecord> nodes = new HashSet<>();
      for (int i = 0; i < PortalGossip.MAX_GOSSIP_COUNT; i++)
        nodes.add(TestHelper.createNodeRecord());
      when(historyJsonRpc.store(any(), any())).thenReturn(false);
      when(historyJsonRpc.getFoundNodes(any(ContentKey.class), anyInt(), any(boolean.class)))
          .thenReturn(nodes);
      mocked.when(() -> PortalGossip.gossip(any(), any(), any(), any())).thenReturn(null);

      final JsonRpcResponse expected =
          new JsonRpcSuccessResponse(
              request.getRequest().getId(),
              new PutContentResult(false, PortalGossip.MAX_GOSSIP_COUNT));
      final JsonRpcResponse actual = method.response(request);
      assertNotNull(actual);
      assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    } catch (Exception e) {
      e.printStackTrace();
    }
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
