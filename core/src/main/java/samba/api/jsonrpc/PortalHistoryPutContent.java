package samba.api.jsonrpc;

import samba.api.jsonrpc.results.PutContentResult;
import samba.api.libary.HistoryLibraryAPI;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalHistoryPutContent implements JsonRpcMethod {

  private final Logger LOG = LoggerFactory.getLogger(PortalHistoryPutContent.class);
  private final HistoryLibraryAPI historyLibraryAPI;

  public PortalHistoryPutContent(final HistoryLibraryAPI historyLibraryAPI) {
    this.historyLibraryAPI = historyLibraryAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_PUT_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Optional<Bytes> contentKeyBytes = getBytesContentKeyFromParameter(requestContext);
      Optional<Bytes> contentBytes = getBytesContentFromParameter(requestContext);

      if (contentKeyBytes.isEmpty() || contentBytes.isEmpty())
        return createJsonRpcInvalidRequestResponse(requestContext);

      ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyBytes.get()).get();

      PutContentResult putContentResult =
          this.historyLibraryAPI.putContent(contentKey, contentBytes.get());

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), putContentResult);
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }

  private Optional<Bytes> getBytesContentFromParameter(JsonRpcRequestContext requestContext) {
    try {
      String bytesHex = requestContext.getRequiredParameter(1, String.class);
      if (bytesHex == null || bytesHex.isEmpty()) {
        return Optional.empty();
      }
      if ("0x".equals(bytesHex)) {
        return Optional.of(Bytes.of(0));
      }
      return Optional.of(Bytes.fromHexString(bytesHex));
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return Optional.empty();
    }
  }

  private Optional<Bytes> getBytesContentKeyFromParameter(JsonRpcRequestContext requestContext) {
    try {
      String bytesHex = requestContext.getRequiredParameter(0, String.class);
      if (bytesHex == null || "0x".equals(bytesHex) || bytesHex.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(Bytes.fromHexString(bytesHex));
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return Optional.empty();
    }
  }
}
