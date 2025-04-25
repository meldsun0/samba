package samba.api.jsonrpc;

import samba.api.HistoryAPI;
import samba.api.jsonrpc.parameters.ContentItemsParameter;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryOffer implements JsonRpcMethod {

  private final HistoryAPI historyAPI;

  public PortalHistoryOffer(final HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_OFFER.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = ParametersUtil.getEnr(requestContext, 0);
      ContentItemsParameter contentItemsParameter =
          requestContext.getRequiredParameter(1, ContentItemsParameter.class);
      if (contentItemsParameter.isNotValid())
        return createJsonRpcInvalidRequestResponse(requestContext);

      List<Bytes> contentKeys = contentItemsParameter.getContentKeys();
      List<Bytes> contents = contentItemsParameter.getContentValues();

      Optional<Bytes> result = historyAPI.offer(enr, contents, contentKeys);

      return result
          .map(bytes -> createSuccessResponse(requestContext, bytes.toHexString()))
          .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
