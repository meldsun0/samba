package samba.services.jsonrpc.methods.history;

import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.extensions.standard.ClientInfoAndCapabilities;
import samba.domain.messages.response.Pong;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.results.PingResult;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class PortalHistoryPing implements JsonRpcMethod {
  private final HistoryNetwork historyNetwork;
  private final Discv5Client discv5Client;

  public PortalHistoryPing(final HistoryNetwork historyNetwork, final Discv5Client discv5Client) {
    this.historyNetwork = historyNetwork;
    this.discv5Client = discv5Client;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_PING.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    String enr;
    try {
      enr = requestContext.getRequiredParameter(0, String.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    //    if (!InputsValidations.isEnrValid(enr))
    //      return createJsonRpcInvalidRequestResponse(requestContext);

    NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);
    if (!historyNetwork.isNodeConnected(
        nodeRecord)) { // TODO should we chekck localRoutingTable or discv5 table ?
      try {
        this.discv5Client.ping(nodeRecord).get();
      } catch (InterruptedException | ExecutionException e) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
    }
    Optional<Pong> pong;
    try {
      pong = historyNetwork.ping(nodeRecord).get();
    } catch (InterruptedException | ExecutionException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (pong.isPresent()) {
      String dataRadius;
      try {
        ExtensionType extensionType =
            ExtensionType.fromValue(pong.get().getPayloadType().getValue());
        switch (extensionType) {
          case CLIENT_INFO_AND_CAPABILITIES -> {
            dataRadius =
                ClientInfoAndCapabilities.getDataRadiusFromSszBytes(pong.get().getPayload())
                    .toHexString();
          }
          case HISTORY_RADIUS -> // TODO when history radius is implemented
              dataRadius = Bytes.EMPTY.toHexString();
          default -> {
            return createJsonRpcInvalidRequestResponse(requestContext);
          }
        }
        UInt64 enrSeq = pong.get().getEnrSeq();
        return new JsonRpcSuccessResponse(
            requestContext.getRequest().getId(),
            new PingResult(enrSeq.bigIntegerValue(), dataRadius));
      } catch (IllegalArgumentException e) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
