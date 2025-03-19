package samba.services.jsonrpc.methods.history;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.jsonrpc.methods.results.FindContentResult;
import samba.services.jsonrpc.methods.results.GetContentResult;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryGetContent implements JsonRpcMethod {
  protected static final Logger LOG = LogManager.getLogger();
  private final HistoryNetwork historyNetwork;
  private final int SEARCH_TIMEOUT = 60;

  public PortalHistoryGetContent(final HistoryNetwork historyNetwork) {
    this.historyNetwork = historyNetwork;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_GET_CONTENT.getMethodName();
  }

  // TODO WIP
  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Optional<Bytes> contentKeyBytes = getContentKeyFromParameter(requestContext);
      if (contentKeyBytes.isEmpty()) return createJsonRpcInvalidRequestResponse(requestContext);
      ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyBytes.get()).get();

      GetContentResult getContentResult;
      Optional<String> content = this.historyNetwork.getLocalContent(contentKey);

      if (content.isPresent()) {
        getContentResult = new GetContentResult(content.get(), false);
      } else {
        Optional<FindContentResult> findContentResult =
            historyNetwork.getContent(contentKey, SEARCH_TIMEOUT).join();
        if (findContentResult.isPresent() && findContentResult.get().getContent() != null) {
          historyNetwork.store(
              contentKey.getSszBytes(), Bytes.fromHexString(findContentResult.get().getContent()));
          getContentResult =
              new GetContentResult(
                  findContentResult.get().getContent(), findContentResult.get().getUtpTransfer());
        } else {
          return new JsonRpcErrorResponse(
              requestContext.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
        }
      }

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), getContentResult);
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }

  private Optional<Bytes> getContentKeyFromParameter(JsonRpcRequestContext requestContext) {
    try {
      String contentKeyHex = requestContext.getRequiredParameter(0, String.class);
      if (contentKeyHex == null || contentKeyHex.isEmpty()) {
        return Optional.empty();
      }
      Bytes contentKeyBytes = Bytes.fromHexString(contentKeyHex);
      return contentKeyBytes.isEmpty() ? Optional.empty() : Optional.of(contentKeyBytes);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return Optional.empty();
    }
  }
}
