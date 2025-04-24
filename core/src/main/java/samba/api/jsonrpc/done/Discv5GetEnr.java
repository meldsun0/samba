package samba.api.jsonrpc.done;

import samba.api.Discv5API;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.HistoryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.Optional;

public class Discv5GetEnr implements JsonRpcMethod {

  private final Discv5API discv5API;

  public Discv5GetEnr(final Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_GET_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.getNodeId(requestContext, 0);
      Optional<String> result = this.discv5API.getEnr(nodeId);

      return result
          .map(enr -> createSuccessResponse(requestContext, enr))
          .orElse(createJsonRpcInvalidRequestResponse(requestContext));
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
