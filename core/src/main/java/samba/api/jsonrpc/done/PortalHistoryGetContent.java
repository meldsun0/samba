package samba.api.jsonrpc.done;

import samba.api.HistoryAPI;
import samba.api.jsonrpc.parameters.ParametersUtil;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import samba.jsonrpc.reponse.RpcErrorType;

public class PortalHistoryGetContent implements JsonRpcMethod {
    protected static final Logger LOG = LogManager.getLogger();

    private final HistoryAPI historyAPI;

    public PortalHistoryGetContent(final HistoryAPI historyAPI) {
        this.historyAPI = historyAPI;
    }

    @Override
    public String getName() {
        return RpcMethod.PORTAL_HISTORY_GET_CONTENT.getMethodName();
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
        try {
            Bytes contentKey = ParametersUtil.getContentKeyBytesFromHexString(requestContext, 0);
            return this.historyAPI.getContent(contentKey).map(content ->
                    createSuccessResponse(requestContext, content)).orElseGet(() -> createJsonRpcInvalidRequestResponse(requestContext,  RpcErrorType.CONTENT_NOT_FOUND_ERROR));
        } catch (Exception e) {
            return createJsonRpcInvalidRequestResponse(requestContext);
        }
    }
}
