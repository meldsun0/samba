package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;

public class PortalHistoryStore implements JsonRpcMethod {

  private HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryStore(final HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_STORE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String contentKey = requestContext.getRequiredParameter(0, String.class);
      String contentValue = requestContext.getRequiredParameter(1, String.class);

      boolean result = this.historyJsonRpcRequests.store(contentKey, contentValue);
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
