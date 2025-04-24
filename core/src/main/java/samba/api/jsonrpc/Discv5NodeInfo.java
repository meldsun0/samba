package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.jsonrpc.results.NodeInfo;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import java.util.Optional;

public class Discv5NodeInfo implements JsonRpcMethod {

  private final Discv5API discv5API;

  public Discv5NodeInfo(final Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_NODE_INFO.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    Optional<NodeInfo> result = this.discv5API.getNodeInfo();
    return result
        .map(nodeInfo -> createSuccessResponse(requestContext, nodeInfo))
        .orElseGet(
            () -> createJsonRpcInvalidRequestResponse(requestContext, RpcErrorType.INTERNAL_ERROR));
  }
}
