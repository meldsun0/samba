package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.HistoryAPI;
import samba.api.jsonrpc.results.RoutingTableInfoResult;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

public class PortalHistoryRoutingTableInfo implements JsonRpcMethod {

  private final HistoryAPI historyAPI;
  private final Discv5API discv5API;

  public PortalHistoryRoutingTableInfo(final HistoryAPI historyAPI, final Discv5API discv5API) {
    this.historyAPI = historyAPI;
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_ROUTING_TABLE_INFO.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    return this.discv5API
        .getNodeInfo()
        .flatMap(
            info ->
                this.historyAPI
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
