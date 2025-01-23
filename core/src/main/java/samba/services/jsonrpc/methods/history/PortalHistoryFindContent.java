package samba.services.jsonrpc.methods.history;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import samba.domain.messages.requests.FindContent;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.network.history.HistoryJsonRpcRequests;
import samba.services.jsonrpc.methods.results.FindContentResult;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class PortalHistoryFindContent implements JsonRpcMethod {
    protected static final Logger LOG = LogManager.getLogger();
    private HistoryJsonRpcRequests historyJsonRpcRequests;


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

         FindContentResult result =   this.historyJsonRpcRequests
                    .findContent(NodeRecordFactory.DEFAULT.fromEnr(enr),
                            new FindContent(Bytes.fromHexString(contentKey))).get().get();

            return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);
        } catch (InterruptedException | RuntimeException | JsonRpcParameter.JsonRpcParameterException |
                 ExecutionException e) {
            return createJsonRpcInvalidRequestResponse(requestContext);
        }
    }
}
