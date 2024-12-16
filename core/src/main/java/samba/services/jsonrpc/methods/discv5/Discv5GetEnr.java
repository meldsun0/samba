package samba.services.jsonrpc.methods.discv5;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.parameters.InputsValidations;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;

public class Discv5GetEnr implements JsonRpcMethod {

  private final Discv5Client discv5Client;

  public Discv5GetEnr(Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_GET_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    String nodeId;
    try {
      nodeId = requestContext.getRequiredParameter(0, String.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (!InputsValidations.isNodeIdValid(nodeId))
      return createJsonRpcInvalidRequestResponse(requestContext);

    nodeId = nodeId.startsWith("0x") ? nodeId.substring(2) : nodeId;
    Optional<String> enr = discv5Client.lookupEnr(UInt256.fromHexString(nodeId));
    if (enr.isPresent()) {
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), enr.get());
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
