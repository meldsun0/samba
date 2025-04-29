package samba.api.jsonrpc;

import samba.api.Discv5API;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

public class Discv5TalkReq implements JsonRpcMethod {

  private final Discv5API discv5API;

  public Discv5TalkReq(final Discv5API discv5API) {
    this.discv5API = discv5API;
  }

  @Override
  public String getName() {
    return RpcMethod.DISCV5_TALK_REQ.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = ParametersUtil.getEnr(requestContext, 0);
      String protocolId = getString(requestContext, 1);
      String talkReqPayload = getString(requestContext, 2);

      return this.discv5API
          .talk(enr, protocolId, talkReqPayload)
          .map(response -> createSuccessResponse(requestContext, response))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    }
  }

  private String getString(JsonRpcRequestContext requestContext, int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    String input = requestContext.getRequiredParameter(index, String.class);
    if ("0x".equals(input) || input.isEmpty()) {
      throw new JsonRpcParameter.JsonRpcParameterException(
          String.format("Invalid contentKey parameter at index %d", index));
    }
    return input;
  }
}
