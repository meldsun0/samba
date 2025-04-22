package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.libary.HistoryLibraryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.Optional;

public class PortalHistoryLookupEnr implements JsonRpcMethod {

  private final HistoryLibraryAPI historyLibraryAPI;

  public PortalHistoryLookupEnr(HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOOKUP_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.getNodeId(requestContext, 0);
      Optional<String> result = historyLibraryAPI.lookupEnr(nodeId);
      return result
          .map(enr -> createSuccessResponse(requestContext, enr))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
