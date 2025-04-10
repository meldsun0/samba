package samba.api.jsonrpc;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;

import java.util.Optional;

import org.apache.tuweni.units.bigints.UInt256;

public class PortalHistoryLookupEnr implements JsonRpcMethod {

  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryLookupEnr(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_LOOKUP_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = ParametersUtil.parseNodeId(requestContext, 0);
      Optional<String> enr = historyJsonRpcRequests.lookupEnr(UInt256.fromHexString(nodeId));
      if (enr.isPresent()) {
        return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), enr.get());
      }
      return createJsonRpcInvalidRequestResponse(requestContext);

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
