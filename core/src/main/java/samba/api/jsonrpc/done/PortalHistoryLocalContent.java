package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.libary.HistoryLibraryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.RpcErrorType;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryLocalContent implements JsonRpcMethod {

  protected static final Logger LOG = LogManager.getLogger();

  private final HistoryLibraryAPI historyLibraryAPI;

  public PortalHistoryLocalContent(final HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOCAL_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Bytes contentKey = ParametersUtil.getBytesFromHexString(requestContext, 0);

      if (contentKey.isEmpty()) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
      Optional<String> result = this.historyLibraryAPI.getLocalContent(contentKey);

      return result
          .map(content -> createSuccessResponse(requestContext, content))
          .orElseGet(
              () ->
                  createJsonRpcInvalidRequestResponse(
                      requestContext, RpcErrorType.CONTENT_NOT_FOUND_ERROR));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
