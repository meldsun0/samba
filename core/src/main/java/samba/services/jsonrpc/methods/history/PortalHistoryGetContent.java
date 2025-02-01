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
import samba.storage.HistoryDB;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public class PortalHistoryGetContent implements JsonRpcMethod {

  private final HistoryNetwork historyNetwork;
  private final HistoryDB historyDB;

  public PortalHistoryGetContent(final HistoryNetwork historyNetwork, final HistoryDB historyDB) {
    this.historyNetwork = historyNetwork;
    this.historyDB = historyDB;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_GET_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    Bytes contentKeyBytes;
    try {
      contentKeyBytes = requestContext.getRequiredParameter(0, Bytes.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (contentKeyBytes.size() == 0) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    try {
      ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyBytes).get();
      Optional<Bytes> content = historyDB.get(contentKey.getContentType(), contentKeyBytes);
      Boolean utpTransfer = false;
      if (content.isEmpty()) {
        Optional<NodeRecord> nodeRecord =
            historyNetwork.findClosestNodeToContentKey(contentKeyBytes);
        if (nodeRecord.isEmpty()) {
          return createJsonRpcInvalidRequestResponse(requestContext);
        }
        FindContentResult findContentResult =
            SafeFuture.supplyAsync(
                    () ->
                        historyNetwork
                            .findContent(nodeRecord.get(), new FindContent(contentKeyBytes))
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
                                  .findContent(node.get(), new FindContent(contentKeyBytes))
                                  .join()
                                  .orElseThrow(
                                      () -> new RuntimeException("Failed to find content")))
                      .join();
              if (searchedNodeResult.getContent() != null) {
                content = Optional.of(Bytes.fromHexString(searchedNodeResult.getContent()));
                utpTransfer = searchedNodeResult.getUtpTransfer();
                return new JsonRpcSuccessResponse(
                    requestContext.getRequest().getId(),
                    new GetContentResult(content.get(), utpTransfer));
              }
            }
          }
          return new JsonRpcErrorResponse(
              requestContext.getRequest().getId(),
              RpcErrorType.CONTENT_NOT_FOUND_ERROR); // new ContentNotFoundError()
        } else {
          content = Optional.of(Bytes.fromHexString(findContentResult.getContent()));
          utpTransfer = findContentResult.getUtpTransfer();
        }
      }
      return new JsonRpcSuccessResponse(
          requestContext.getRequest().getId(), new GetContentResult(content.get(), utpTransfer));
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
