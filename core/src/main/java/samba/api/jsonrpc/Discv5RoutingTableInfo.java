package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.jsonrpc.results.RoutingTableInfoResult;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Discv5RoutingTableInfo implements JsonRpcMethod {

  protected static final Logger LOG = LogManager.getLogger();

  private final Discv5API discv5API;

  public Discv5RoutingTableInfo(final Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_ROUTING_TABLE_INFO.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    return this.discv5API
        .getNodeInfo()
        .flatMap(
            info ->
                this.discv5API
                    .getRoutingTable()
                    .map(
                        table -> {
                          RoutingTableInfoResult result =
                              new RoutingTableInfoResult(info.getNodeId(), table);
                          return createSuccessResponse(requestContext, result);
                        }))
        .orElseGet(
            () ->
                createJsonRpcInvalidRequestResponse(requestContext, RpcErrorType.INVALID_REQUEST));
  }
}
