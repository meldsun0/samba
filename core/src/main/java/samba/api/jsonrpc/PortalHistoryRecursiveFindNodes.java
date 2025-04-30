package samba.api.jsonrpc;

import samba.api.HistoryAPI;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

public class PortalHistoryRecursiveFindNodes implements JsonRpcMethod {

  private final HistoryAPI historyAPI;

  public PortalHistoryRecursiveFindNodes(final HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_RECURSIVE_FIND_NODES.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.getNodeId(requestContext, 0);
      return this.historyAPI
          .recursiveFindNodes(nodeId)
          .map(content -> createSuccessResponse(requestContext, content))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
