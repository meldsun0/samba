package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5FindNode implements JsonRpcMethod {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5FindNode.class);

  private final Discv5API discv5API;

  public Discv5FindNode(Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_FIND_NODE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = ParametersUtil.getEnr(requestContext, 0);
      Set<Integer> distances = ParametersUtil.getDistances(requestContext, 1);
      Optional<List<String>> result = this.discv5API.findNodes(enr, distances);
      return result
          .map(list -> createSuccessResponse(requestContext, list))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
