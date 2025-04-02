package samba.services.jsonrpc.methods.history;

import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.extensions.PortalExtension;
import samba.domain.messages.extensions.standard.ClientInfoAndCapabilities;
import samba.domain.messages.requests.Ping;
import samba.domain.messages.response.Pong;
import samba.domain.types.unsigned.UInt16;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcErrorResponse;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.jsonrpc.reponse.RpcErrorType;
import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.results.PingResult;
import samba.services.jsonrpc.methods.schemas.ClientInfoAndCapabilitiesJson;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    Optional<String> payloadType;
    Optional<String> payload;
    try {
      enr = requestContext.getRequiredParameter(0, String.class);
      payloadType = requestContext.getOptionalParameter(1, String.class);
      payload = requestContext.getOptionalParameter(2, String.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    Optional<UInt16> parsedPayloadType = Optional.empty();
    try {
      if (payloadType.isPresent()) {
        parsedPayloadType = Optional.of(UInt16.valueOf(Integer.parseInt(payloadType.get())));
        if (!PortalExtension.DEFAULT_CAPABILITIES.contains(parsedPayloadType.get())
            && parsedPayloadType.get() != ExtensionType.ERROR.getExtensionCode()) {
          return new JsonRpcErrorResponse(
              requestContext.getRequest().getId(), RpcErrorType.PAYLOAD_TYPE_NOT_SUPPORTED_ERROR);
        }
      }
    } catch (IllegalArgumentException e) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.PAYLOAD_TYPE_NOT_SUPPORTED_ERROR);
    }

    Optional<PortalExtension> payloadExtension = Optional.empty();
    ObjectMapper objectMapper = new ObjectMapper();
    if (payload.isPresent() && parsedPayloadType.isPresent()) {
      ExtensionType extensionType = ExtensionType.fromValue(parsedPayloadType.get());

      switch (extensionType) {
        case CLIENT_INFO_AND_CAPABILITIES -> {
          try {
            ClientInfoAndCapabilitiesJson payloadJson =
                objectMapper.readValue(payload.get(), ClientInfoAndCapabilitiesJson.class);
            payloadExtension =
                Optional.of(
                    new ClientInfoAndCapabilities(
                        payloadJson.getClientInfo(),
                        payloadJson.getDataRadius(),
                        payloadJson.getCapabilities()));
          } catch (Exception e) {
            return new JsonRpcErrorResponse(
                requestContext.getRequest().getId(), RpcErrorType.FAILED_TO_DECODE_PAYLOAD_ERROR);
          }
        }
        case HISTORY_RADIUS -> {
          // TODO: Implement history radius handling
        }
        default -> {
          return new JsonRpcErrorResponse(
              requestContext.getRequest().getId(), RpcErrorType.PAYLOAD_TYPE_NOT_SUPPORTED_ERROR);
        }
      }
    } else if (payload.isPresent() && payloadType.isEmpty()) {
      return new JsonRpcErrorResponse(
          requestContext.getRequest().getId(), RpcErrorType.PAYLOAD_TYPE_REQUIRED_ERROR);
    }
    //    if (!InputsValidations.isEnrValid(enr))
    //      return createJsonRpcInvalidRequestResponse(requestContext);

    NodeRecord nodeRecord = NodeRecordFactory.DEFAULT.fromEnr(enr);
    if (!historyNetwork.isNodeConnected(
        nodeRecord)) { // TODO should we check localRoutingTable or discv5 table ?
      try {
        this.discv5Client.ping(nodeRecord).get();
      } catch (InterruptedException | ExecutionException e) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
    }

    Optional<Pong> pong;
    try {
      if (payloadType.isEmpty()) {
        pong = historyNetwork.ping(nodeRecord).get();
      } else if (payloadType.isPresent()
          && payload
              .isEmpty()) { // TODO add option to send default payload when new extesnions are added
        pong = historyNetwork.ping(nodeRecord).get();
      } else {
        pong =
            historyNetwork
                .ping(
                    nodeRecord,
                    new Ping(
                        nodeRecord.getSeq(),
                        parsedPayloadType.get(),
                        payloadExtension.get().getSszBytes()))
                .get();
      }
    } catch (InterruptedException | ExecutionException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (pong.isPresent()) {
      String extensionJson;
      try {
        ExtensionType extensionType = ExtensionType.fromValue(pong.get().getPayloadType());
        switch (extensionType) {
          case CLIENT_INFO_AND_CAPABILITIES -> {
            ClientInfoAndCapabilities clientInfoAndCapabilities =
                ClientInfoAndCapabilities.fromSszBytes(pong.get().getPayload());
            extensionJson =
                objectMapper.writeValueAsString(
                    new ClientInfoAndCapabilitiesJson(
                        clientInfoAndCapabilities.getClientInfo(),
                        clientInfoAndCapabilities.getDataRadius(),
                        clientInfoAndCapabilities.getCapabilities()));
          }
          case HISTORY_RADIUS -> { // TODO when history radius is implemented
            return createJsonRpcInvalidRequestResponse(requestContext);
          }
          default -> {
            return createJsonRpcInvalidRequestResponse(requestContext);
          }
        }
        UInt64 enrSeq = pong.get().getEnrSeq();
        return new JsonRpcSuccessResponse(
            requestContext.getRequest().getId(),
            new PingResult(enrSeq.bigIntegerValue(), pong.get().getPayloadType(), extensionJson));
      } catch (IllegalArgumentException | JsonProcessingException e) {
        return createJsonRpcInvalidRequestResponse(requestContext);
      }
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
