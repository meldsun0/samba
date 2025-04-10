package samba.api.jsonrpc;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryStore implements JsonRpcMethod {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PortalHistoryStore(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_STORE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = Bytes.fromHexString(requestContext.getRequiredParameter(0, String.class));
      Bytes contentValue =
          Bytes.fromHexString(requestContext.getRequiredParameter(1, String.class));
      if (contentKey.isEmpty()) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }

      boolean result = this.historyNetworkInternalAPI.store(contentKey, contentValue);
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
