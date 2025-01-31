package samba.services.jsonrpc.methods.history;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcResponse;
import samba.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.storage.HistoryDB;

import org.apache.tuweni.bytes.Bytes;

public class PortalHistoryStore implements JsonRpcMethod {

  private final HistoryDB historyDB;

  public PortalHistoryStore(final HistoryDB historyDB) {
    this.historyDB = historyDB;
  }

  @Override
  public String getName() {
    return RpcMethod.PORTAL_HISTORY_STORE.getMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
    Bytes contentKey;
    Bytes contentValue;
    try {
      contentKey = requestContext.getRequiredParameter(0, Bytes.class);
      contentValue = requestContext.getRequiredParameter(1, Bytes.class);
    } catch (JsonRpcParameter.JsonRpcParameterException e) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (contentKey.size() == 0 || contentValue.size() == 0) {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
    if (historyDB.saveContent(contentKey, contentValue)) {
      return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), true);
    } else {
      return createJsonRpcInvalidRequestResponse(requestContext);
    }
  }
}
