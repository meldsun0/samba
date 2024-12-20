package samba.services.jsonrpc.methods.history;

import samba.domain.messages.requests.FindNodes;
import samba.domain.messages.response.Nodes;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryJsonRpcRequests;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryFindNodes implements JsonRpcMethod {
  protected static final Logger LOG = LogManager.getLogger();
  private HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryFindNodes(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_FIND_NODES.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    String enr;
    Set<Integer> distances;
    try {
      enr = requestContext.getRequiredParameter(0, String.class);
      distances =
          Arrays.stream(requestContext.getRequiredParameter(1, Integer[].class))
              .collect(Collectors.toSet());
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }

    Optional<Nodes> nodes;
    try {
      nodes =
          this.historyJsonRpcRequests
              .findNodes(NodeRecordFactory.DEFAULT.fromEnr(enr), new FindNodes(distances))
              .get();

    } catch (InterruptedException | ExecutionException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }

    if (nodes.isPresent()) {
      return new JsonRpcSuccessResponse(
          requestContext.getRequest().getId(), nodes.get().getEnrsWithENRPerItem());
    }
    return createJsonRpcInvalidRequestResponse(requestContext);
  }
}
