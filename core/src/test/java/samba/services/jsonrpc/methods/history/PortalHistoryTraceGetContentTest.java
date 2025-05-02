package samba.services.jsonrpc.methods.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import samba.api.HistoryAPIClient;
import samba.api.jsonrpc.PortalHistoryTraceGetContent;
import samba.api.jsonrpc.results.TraceGetContentResult;
import samba.api.jsonrpc.schemas.TraceResultObjectJson;
import samba.domain.content.ContentKey;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcRequest;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.crypto.Hash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalHistoryTraceGetContentTest {

  private final String JSON_RPC_VERSION = "2.0";
  private final String PORTAL_HISTORY_TRACE_GET_CONTENT = "portal_historyTraceGetContent";
  private PortalHistoryTraceGetContent method;
  private HistoryNetwork historyJsonRpc;
  private TimeProvider timeProvider;

  @BeforeEach
  public void before() {
    this.historyJsonRpc = mock(HistoryNetwork.class);
    this.timeProvider = mock(TimeProvider.class);
    method = new PortalHistoryTraceGetContent(new HistoryAPIClient(historyJsonRpc), timeProvider);
    when(timeProvider.getTimeInMillis()).thenReturn(UInt64.ZERO);
  }

  @Test
  public void shouldReturnCorrectMethodName() {
    assertThat(method.getName()).isEqualTo(PORTAL_HISTORY_TRACE_GET_CONTENT);
  }

  @Test
  public void shouldReturnCorrectResultFromLocalStorage() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_TRACE_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    final String foundData = "0x1234";
    final TraceResultObjectJson traceResult =
        new TraceResultObjectJson(
            UInt256.ZERO,
            UInt256.fromBytes(
                Hash.sha256(
                    Bytes.fromHexString(
                        "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"))),
            UInt256.ZERO,
            new HashMap<>(),
            new HashMap<>(),
            0L,
            List.of());
    when(historyJsonRpc.getLocalContent(any(ContentKey.class))).thenReturn(Optional.of(foundData));
    when(historyJsonRpc.getLocalNodeId()).thenReturn(UInt256.ZERO);

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(), new TraceGetContentResult(foundData, false, traceResult));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnCorrectResultFromNetwork() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_TRACE_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    final String foundData = "0x1234";
    final TraceResultObjectJson traceResult =
        new TraceResultObjectJson(
            UInt256.ZERO,
            UInt256.ONE,
            UInt256.ZERO,
            new HashMap<>(),
            new HashMap<>(),
            0L,
            List.of());
    when(historyJsonRpc.traceGetContent(any(ContentKey.class), anyInt(), anyLong()))
        .thenReturn(Optional.of(new TraceGetContentResult(foundData, false, traceResult)));

    final JsonRpcResponse expected =
        new JsonRpcSuccessResponse(
            request.getRequest().getId(), new TraceGetContentResult(foundData, false, traceResult));
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnContentNotFoundErrorWithTrace() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION,
                PORTAL_HISTORY_TRACE_GET_CONTENT,
                new Object[] {
                  "0x01720704f3aa11c53cf344ea069db95cecb81ad7453c8f276b2a1062979611f09c"
                }));
    when(historyJsonRpc.getLocalContent(any(ContentKey.class))).thenReturn(Optional.empty());
    when(historyJsonRpc.traceGetContent(any(ContentKey.class), anyInt(), anyLong()))
        .thenReturn(Optional.empty());

    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(
            request.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR_WITH_TRACE);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  @Test
  public void shouldReturnInvalidResultAsNoParameterisSent() {
    final JsonRpcRequestContext request =
        new JsonRpcRequestContext(
            new JsonRpcRequest(
                JSON_RPC_VERSION, PORTAL_HISTORY_TRACE_GET_CONTENT, new Object[] {}));
    final JsonRpcResponse expected =
        new JsonRpcErrorResponse(request.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    final JsonRpcResponse actual = method.response(request);
    assertNotNull(actual);
    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }
}
