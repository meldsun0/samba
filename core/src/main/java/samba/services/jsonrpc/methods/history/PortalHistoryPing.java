package samba.services.jsonrpc.methods.history;

import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryNetwork;
import samba.services.jsonrpc.methods.parameters.InputsValidations;
import samba.services.jsonrpc.methods.results.PingResult;

import java.math.BigInteger;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes32;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryPing implements JsonRpcMethod {

  private HistoryNetwork historyNetwork;

  public PortalHistoryPing(HistoryNetwork historyNetwork) {
    this.historyNetwork = historyNetwork;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_PING.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = requestContext.getRequiredParameter(0, String.class);
      final NodeRecord nodeRecord = buildNodeRecord(enr);

      Ping pingMessage = new Ping(nodeRecord.getSeq(), Bytes32.random());
      Optional<Pong> pong = historyNetwork.ping2(nodeRecord);

      String dataRadius = pong.get().getCustomPayload().toHexString();
      BigInteger enrSeq = pong.get().getEnrSeq().bigIntegerValue();

      PingResult pingResult = new PingResult(enrSeq, dataRadius);

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), pingResult);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
    }
  }

  private void validateInput(String nodeId) throws JsonRpcParameter.JsonRpcParameterException {
    if (!InputsValidations.isValidateNodeId(nodeId)) {
      throw new JsonRpcParameter.JsonRpcParameterException(RpcErrorType.INVALID_REQUEST.toString());
    }
  }

  private NodeRecord buildNodeRecord(final String enr) {
    final NodeRecordFactory nodeRecordFactory = NodeRecordFactory.DEFAULT;
    return nodeRecordFactory.fromBase64(
        enr.startsWith("enr:") ? enr.substring("enr:".length()) : enr);
  }
}
