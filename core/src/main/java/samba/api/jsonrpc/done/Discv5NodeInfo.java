package samba.api.jsonrpc.done;

import samba.api.jsonrpc.results.NodeInfo;
import samba.api.libary.HistoryLibraryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import java.util.Optional;

public class Discv5NodeInfo implements JsonRpcMethod {

  private final HistoryLibraryAPI historyLibraryAPI;

  public Discv5NodeInfo(final HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_NODE_INFO.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    Optional<NodeInfo> result = this.historyLibraryAPI.discv5GetNodeInfo();
    return result
        .map(nodeInfo -> createSuccessResponse(requestContext, nodeInfo))
        .orElseGet(
            () -> createJsonRpcInvalidRequestResponse(requestContext, RpcErrorType.INTERNAL_ERROR));
  }
}
