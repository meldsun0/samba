package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.HistoryAPI;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PortalHistoryFindNodes implements JsonRpcMethod {
  protected static final Logger LOG = LogManager.getLogger();
  private final HistoryAPI historyAPI;

  public PortalHistoryFindNodes(HistoryAPI historyAPI) {
    this.historyAPI = historyAPI;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_FIND_NODES.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = ParametersUtil.getEnr(requestContext, 0);
      Set<Integer> distances = ParametersUtil.getDistances(requestContext, 1);
      List<String> result = this.historyAPI.findNodes(enr, distances);
      return createSuccessResponse(requestContext, result);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
