package samba.api.jsonrpc;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;

public class PortalHistoryAddEnr implements JsonRpcMethod {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PortalHistoryAddEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
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

    historyNetworkInternalAPI.addEnr(enr);

    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
