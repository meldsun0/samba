package samba.api.jsonrpc;

import samba.api.jsonrpc.parameters.InputsValidations;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;

public class PortalHistoryDeleteEnr implements JsonRpcMethod {

  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryDeleteEnr(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_DELETE_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    String nodeId;
    try {
      nodeId = requestContext.getRequiredParameter(0, String.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (!InputsValidations.isNodeIdValid(nodeId)) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    boolean wasRemoved = this.historyJsonRpcRequests.deleteEnr(nodeId);

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), wasRemoved);
  }
}
