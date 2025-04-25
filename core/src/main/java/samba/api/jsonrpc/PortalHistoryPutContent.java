package samba.api.jsonrpc;

import samba.api.HistoryAPI;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.jsonrpc.results.PutContentResult;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalHistoryPutContent implements JsonRpcMethod {

  private final Logger LOG = LoggerFactory.getLogger(PortalHistoryPutContent.class);
  private final HistoryAPI historyAPI;

  public PortalHistoryPutContent(final HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_PUT_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = ParametersUtil.getContentKeyBytesFromHexString(requestContext, 0);
      Bytes contentBytes = ParametersUtil.getBytesFromHexString(requestContext, 1);

      PutContentResult putContentResult = this.historyAPI.putContent(contentKey, contentBytes);

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), putContentResult);
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
