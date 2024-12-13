package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryNetwork;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryAddEnr implements JsonRpcMethod {

  private HistoryNetwork historyNetwork;

  public PortalHistoryAddEnr(HistoryNetwork historyNetwork) {
    this.historyNetwork = historyNetwork;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_ADD_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = requestContext.getRequiredParameter(0, String.class);
      final NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);
      this.historyNetwork.addNodeRecordToRoutingTable(nodeRecord);
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    }
  }
}
