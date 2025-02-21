package samba.services.jsonrpc.methods.history;

import samba.domain.content.ContentKey;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryJsonRpcRequests;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryLocalContent implements JsonRpcMethod {

  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryLocalContent(final HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOCAL_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = Bytes.fromHexString(requestContext.getRequiredParameter(0, String.class));

      if (contentKey.isEmpty()) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }

      Optional<String> result =
          this.historyJsonRpcRequests.getLocalContent(ContentKey.decode(contentKey));
      if (result.isEmpty()) {
        return new JsonRpcErrorResponse(
            requestContext.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
      }
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result.get());

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
