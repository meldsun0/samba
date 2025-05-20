package samba.api.jsonrpc;

import samba.api.HistoryAPI;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.time.TimeProvider;

public class PortalHistoryTraceGetContent implements JsonRpcMethod {

  private final HistoryAPI historyAPI;
  private final TimeProvider timeProvider;

  public PortalHistoryTraceGetContent(
      final HistoryAPI historyAPI, final TimeProvider timeProvider) {
    this.historyAPI = historyAPI;
    this.timeProvider = timeProvider;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_TRACE_GET_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = ParametersUtil.getContentKeyBytesFromHexString(requestContext, 0);
      return this.historyAPI
          .traceGetContent(contentKey, timeProvider.getTimeInMillis().longValue())
          .map(content -> createSuccessResponse(requestContext, content))
          .orElseGet(
              () ->
                  createJsonRpcInvalidRequestResponse(
                      requestContext, RpcErrorType.CONTENT_NOT_FOUND_ERROR_WITH_TRACE));
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
