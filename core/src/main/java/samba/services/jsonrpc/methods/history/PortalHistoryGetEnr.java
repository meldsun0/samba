package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryNetwork;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public class PortalHistoryGetEnr implements JsonRpcMethod {

  private static final Logger LOG = LogManager.getLogger();

  private HistoryNetwork historyNetwork;

  public PortalHistoryGetEnr(HistoryNetwork historyNetwork) {
    this.historyNetwork = historyNetwork;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_GET_ENR.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String nodeId = requestContext.getRequiredParameter(0, String.class);
      Optional<NodeRecord> nodeRecordFound =
          this.historyNetwork.getENRFromRoutingTable(Bytes.fromHexString(nodeId));
      if (nodeRecordFound.isPresent()) {
        return new JsonRpcSuccessResponse(
            requestContext.getRequest().getId(), nodeRecordFound.map(NodeRecord::asEnr));
      } else {
        return new JsonRpcErrorResponse(
            requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
      }

    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    }
  }
}
