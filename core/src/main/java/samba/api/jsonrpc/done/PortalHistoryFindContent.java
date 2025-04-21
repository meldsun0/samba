package samba.api.jsonrpc.done;

import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.api.jsonrpc.results.FindContentResult;
import samba.api.libary.HistoryLibraryAPI;
import samba.domain.messages.requests.FindContent;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;

public class PortalHistoryFindContent implements JsonRpcMethod {
    protected static final Logger LOG = LogManager.getLogger();
    private final HistoryLibraryAPI historyLibraryAPI;

    public PortalHistoryFindContent(HistoryLibraryAPI historyLibraryAPI) {
        this.historyLibraryAPI = historyLibraryAPI;
    }

    @Override
    public String getName() {
        return RpcMethod.PORTAL_HISTORY_FIND_CONTENT.getMethodName();
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
        try {
            String enr = ParametersUtil.getEnr(requestContext, 0);
            Bytes contentKey = ParametersUtil.getBytesFromHexString(requestContext, 1);

            Optional<FindContentResult> findContentResult = this.historyLibraryAPI.findContent(enr, contentKey);

            return findContentResult.map(value -> createSuccessResponse(requestContext, findContentResult.get()))
                    .orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext));
            
        } catch (JsonRpcParameter.JsonRpcParameterException e) {
            return createJsonRpcInvalidRequestResponse(requestContext);
        }
    }
}
