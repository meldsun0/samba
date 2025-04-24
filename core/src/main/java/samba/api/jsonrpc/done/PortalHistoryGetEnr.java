package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.HistoryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.Optional;

public class PortalHistoryGetEnr implements JsonRpcMethod {

  private final HistoryAPI historyAPI;

  public PortalHistoryGetEnr(HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_GET_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.getNodeId(requestContext, 0);
      Optional<String> enr = this.historyAPI.getEnr(nodeId);

      return enr.map(value -> createSuccessResponse(requestContext, enr.get()))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
