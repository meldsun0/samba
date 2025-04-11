package samba.api.jsonrpc;

import samba.domain.content.ContentKey;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryLocalContent implements JsonRpcMethod {

  protected static final Logger LOG = LogManager.getLogger();

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PortalHistoryLocalContent(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOCAL_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = Bytes.fromHexString(requestContext.getRequiredParameter(0, String.class));

      if (contentKey.isEmpty()) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }

      Optional<String> result =
          this.historyNetworkInternalAPI.getLocalContent(ContentKey.decode(contentKey));

      if (result.isEmpty()) {
        return new JsonRpcErrorResponse(
            requestContext.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
      }
      if ("0x00".equals(result.get())) {
        return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), "0x");
      }
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result.get());

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
