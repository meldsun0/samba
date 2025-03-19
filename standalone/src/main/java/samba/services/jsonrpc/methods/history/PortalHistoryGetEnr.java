package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;
import samba.services.jsonrpc.methods.parameters.InputsValidations;

import java.util.Optional;

public class PortalHistoryGetEnr implements JsonRpcMethod {

  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryGetEnr(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
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
    Optional<String> enr = this.historyJsonRpcRequests.getEnr(nodeId);

    if (enr.isPresent()) {
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), enr.get());
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
