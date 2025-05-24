package samba.api.jsonrpc;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

public class PortalBeaconStore implements JsonRpcMethod {

  public PortalBeaconStore() {}

  @Override
  public String getName() {
    return RpcMethod.PORTAL_BEACON_STORE.getMethodName();
  }

  // TODO: migrate to beacon network and add actual summary store functionality
  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
  }
}
