package samba.services.jsonrpc.methods.history;

import samba.domain.messages.requests.FindContent;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.HistoryJsonRpcRequests;
import samba.services.jsonrpc.methods.results.FindContentResult;

import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryFindContent implements JsonRpcMethod {
  protected static final Logger LOG = LogManager.getLogger();
  private final HistoryJsonRpcRequests historyJsonRpcRequests;

  public PortalHistoryFindContent(HistoryJsonRpcRequests historyJsonRpcRequests) {
    this.historyJsonRpcRequests = historyJsonRpcRequests;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_FIND_CONTENT.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    try {
      String enr = requestContext.getRequiredParameter(0, String.class);
      String contentKey = requestContext.getRequiredParameter(1, String.class);

      FindContentResult result =
          this.historyJsonRpcRequests
              .findContent(
                  NodeRecordFactory.DEFAULT.fromEnr(enr),
                  new FindContent(Bytes.fromHexString(contentKey)))
              .get()
              .get();

      ObjectMapper objectMapper = new ObjectMapper();
      String json = objectMapper.writeValueAsString(result);
      LOG.info("FindContentResult: {}", json);

      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);
    } catch (InterruptedException
        | RuntimeException
        | JsonRpcParameter.JsonRpcParameterException
        | ExecutionException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
