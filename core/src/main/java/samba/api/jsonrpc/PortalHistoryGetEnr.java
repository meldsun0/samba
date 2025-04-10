package samba.api.jsonrpc;

import samba.api.jsonrpc.parameters.InputsValidations;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

public class PortalHistoryGetEnr implements JsonRpcMethod {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PortalHistoryGetEnr(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_GET_ENR.getMethodName();
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
    Optional<String> enr = this.historyNetworkInternalAPI.getEnr(nodeId);

    if (enr.isPresent()) {
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), enr.get());
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
