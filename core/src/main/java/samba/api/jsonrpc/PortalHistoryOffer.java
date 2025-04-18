package samba.api.jsonrpc;

import samba.api.jsonrpc.parameters.ContentItemsParameter;
import samba.domain.messages.requests.Offer;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryOffer implements JsonRpcMethod {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public PortalHistoryOffer(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_OFFER.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = requestContext.getRequiredParameter(0, String.class);
      ContentItemsParameter contentItemsParameter =
          requestContext.getRequiredParameter(1, ContentItemsParameter.class);
      if (contentItemsParameter.isNotValid())
        return createJsonRpcInvalidRequestResponse(requestContext);

      List<Bytes> contentKeys = contentItemsParameter.getContentKeys();
      List<Bytes> content = contentItemsParameter.getContentValues();

      final NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);

      Optional<Bytes> contentKeysBitList =
          this.historyNetworkInternalAPI.offer(nodeRecord, content, new Offer(contentKeys)).get();

      if (contentKeysBitList.isEmpty()) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
      return new JsonRpcSuccessResponse(
          requestContext.getRequest().getId(), contentKeysBitList.get().toHexString());

    } catch (JsonRpcParameter.JsonRpcParameterException
        | InterruptedException
        | ExecutionException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
