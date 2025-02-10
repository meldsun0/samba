package samba.services.jsonrpc.methods.history;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.domain.messages.requests.FindContent;
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
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalHistoryGetContent implements JsonRpcMethod {
  protected static final Logger LOG = LogManager.getLogger();
  private final HistoryNetwork historyNetwork;

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

      Optional<Bytes> content = this.historyNetwork.getContent(contentKey);

      Boolean utpTransfer = false;
      if (content.isEmpty()) {
        Optional<NodeRecord> nodeRecord =
            historyNetwork.findClosestNodeToContentKey(contentKeyBytes.get());
        if (nodeRecord.isEmpty()) return createJsonRpcInvalidRequestResponse(requestContext);

        FindContentResult findContentResult =
            SafeFuture.supplyAsync(
                    () ->
                        historyNetwork
                            .findContent(nodeRecord.get(), new FindContent(contentKeyBytes.get()))
                            .join()
                            .orElseThrow(() -> new RuntimeException("Failed to find content")))
                .join();
        if (findContentResult.getContent() == null) {
          // TODO Potential indefinite lookup?
          for (String enr : findContentResult.getEnrs()) {
            Optional<NodeRecord> node = historyNetwork.nodeRecordFromEnr(enr);
            if (node.isPresent()) {
              FindContentResult searchedNodeResult =
                  SafeFuture.supplyAsync(
                          () ->
                              historyNetwork
                                  .findContent(node.get(), new FindContent(contentKeyBytes.get()))
                                  .join()
                                  .orElseThrow(
                                      () -> new RuntimeException("Failed to find content")))
                      .join();
              if (searchedNodeResult.getContent() != null) {
                content = Optional.of(Bytes.fromHexString(searchedNodeResult.getContent()));
                utpTransfer = searchedNodeResult.getUtpTransfer();
                return new JsonRpcSuccessResponse(
                    requestContext.getRequest().getId(),
                    new GetContentResult(content.get().toHexString(), utpTransfer));
              }
            }
          }

          return new JsonRpcErrorResponse(
              requestContext.getRequest().getId(), RpcErrorType.CONTENT_NOT_FOUND_ERROR);
        } else {

          content = Optional.of(Bytes.fromHexString(findContentResult.getContent()));
          utpTransfer = findContentResult.getUtpTransfer();
        }
      }
      return new JsonRpcSuccessResponse(
          requestContext.getRequest().getId(),
          new GetContentResult(content.get().toHexString(), utpTransfer));
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
