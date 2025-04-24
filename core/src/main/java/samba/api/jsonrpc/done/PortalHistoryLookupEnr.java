package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.HistoryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.Optional;

public class PortalHistoryLookupEnr implements JsonRpcMethod {

  private final HistoryAPI historyAPI;

  public PortalHistoryLookupEnr(HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOOKUP_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.getNodeId(requestContext, 0);
      Optional<String> result = historyAPI.lookupEnr(nodeId);
      return result
          .map(enr -> createSuccessResponse(requestContext, enr))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
