package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

public class Discv5AddEnr implements JsonRpcMethod {

  private final Discv5API discv5API;

  public Discv5AddEnr(final Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_ADD_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = ParametersUtil.getEnr(requestContext, 0);
      boolean result = this.discv5API.addEnr(enr);

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
