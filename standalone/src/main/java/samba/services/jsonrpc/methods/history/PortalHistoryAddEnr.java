package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;

public class PortalHistoryAddEnr implements JsonRpcMethod {

  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryAddEnr(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_ADD_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    String enr;
    try {
      enr = requestContext.getRequiredParameter(0, String.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    //    if (!InputsValidations.isEnrValid(enr))
    //      return createJsonRpcInvalidRequestResponse(requestContext);

    historyJsonRpcRequests.addEnr(enr);

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
