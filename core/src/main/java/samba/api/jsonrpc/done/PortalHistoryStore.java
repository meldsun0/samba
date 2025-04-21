package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.libary.HistoryLibraryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryStore implements JsonRpcMethod {

  private final HistoryLibraryAPI historyLibraryAPI;

  public PortalHistoryStore(final HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_STORE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = ParametersUtil.getBytesFromHexString(requestContext, 0);
      Bytes contentValue = ParametersUtil.getBytesFromHexString(requestContext, 1);

      boolean result = this.historyLibraryAPI.store(contentKey, contentValue);
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
