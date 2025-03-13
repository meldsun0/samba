package samba.services.jsonrpc.methods.parameters;

import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;

public class ParametersUtil {

  public static String parseNodeId(final JsonRpcRequestContext requestContext, final int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    String nodeId = requestContext.getRequiredParameter(index, String.class);
    if (!InputsValidations.isNodeIdValid(nodeId))
      throw new JsonRpcParameter.JsonRpcParameterException(
          String.format("Invalid nodeId parameter at index %d", index));
    return nodeId.startsWith("0x") ? nodeId.substring(2) : nodeId;
  }
}
