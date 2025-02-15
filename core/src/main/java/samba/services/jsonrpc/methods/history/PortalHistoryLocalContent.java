package samba.services.jsonrpc.methods.history;

import samba.domain.content.ContentKey;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryJsonRpcRequests;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryLocalContent implements JsonRpcMethod {

  private HistoryJsonRpcRequests historyJsonRpcRequests;

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
        return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), false);
      }

      String result = this.historyJsonRpcRequests.getLocalContent(ContentKey.decode(contentKey));
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
