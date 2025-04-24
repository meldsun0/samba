package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.jsonrpc.results.NodeInfo;
import samba.api.libary.HistoryLibraryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import java.net.InetSocketAddress;
import java.util.Optional;

public class Discv5UpdateNodeInfo implements JsonRpcMethod {

  private final HistoryLibraryAPI historyLibraryAPI;

  public Discv5UpdateNodeInfo(HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_UPDATE_NODE_INFO.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      final InetSocketAddress socketAddr = ParametersUtil.getSocketAddress(requestContext, 0);

      final boolean isTCP = requestContext.getRequiredParameter(1, Boolean.class);

      Optional<NodeInfo> result = this.historyLibraryAPI.discv5UpdateNodeInfo(socketAddr, isTCP);
      return result
          .map(nodeIndo -> createSuccessResponse(requestContext, nodeIndo))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    }
  }
}
