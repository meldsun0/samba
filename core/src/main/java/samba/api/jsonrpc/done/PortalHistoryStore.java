package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.HistoryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryStore implements JsonRpcMethod {

  private final HistoryAPI historyAPI;

  public PortalHistoryStore(final HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_STORE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = ParametersUtil.getContentKeyBytesFromHexString(requestContext, 0);
      Bytes contentValue = ParametersUtil.getContentBytesFromHexString(requestContext, 1);

      boolean result = this.historyAPI.store(contentKey, contentValue);
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), false);
    }
  }
}
