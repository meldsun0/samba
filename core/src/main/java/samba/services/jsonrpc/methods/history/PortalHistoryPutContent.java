package samba.services.jsonrpc.methods.history;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.PortalGossip;
import samba.network.history.HistoryNetwork;
import samba.services.jsonrpc.methods.results.PutContentResult;

import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortalHistoryPutContent implements JsonRpcMethod {

  private final Logger LOG = LoggerFactory.getLogger(PortalHistoryPutContent.class);
  private final HistoryNetwork historyNetwork;

  public PortalHistoryPutContent(final HistoryNetwork historyNetwork) {
    this.historyNetwork = historyNetwork;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_PUT_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      Optional<Bytes> contentKeyBytes = getBytesFromParameter(requestContext);
      Optional<Bytes> contentBytes = getBytesFromParameter(requestContext);
      if (contentKeyBytes.isEmpty() || contentBytes.isEmpty())
        return createJsonRpcInvalidRequestResponse(requestContext);
      ContentKey contentKey = ContentUtil.createContentKeyFromSszBytes(contentKeyBytes.get()).get();
      boolean storedLocally = this.historyNetwork.store(contentKeyBytes.get(), contentBytes.get());

      Set<NodeRecord> nodes =
          this.historyNetwork.getFoundNodes(contentKey, PortalGossip.MAX_GOSSIP_COUNT, true);
      PortalGossip.gossip(this.historyNetwork, nodes, contentKeyBytes.get(), contentBytes.get());

      PutContentResult putContentResult = new PutContentResult(storedLocally, nodes.size());

      LOG.info(
          "Put content: {} stored locally: {} nodes: {}", contentKey, storedLocally, nodes.size());
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), putContentResult);
    } catch (Exception e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }

  private Optional<Bytes> getBytesFromParameter(JsonRpcRequestContext requestContext) {
    try {
      String bytesHex = requestContext.getRequiredParameter(0, String.class);
      if (bytesHex == null || bytesHex.isEmpty()) {
        return Optional.empty();
      }
      Bytes bytes = Bytes.fromHexString(bytesHex);
      return bytes.isEmpty() ? Optional.empty() : Optional.of(bytes);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return Optional.empty();
    }
  }
}
