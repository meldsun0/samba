package samba.jsonrpc.handler.processor;


import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.jsonrpc.reponse.JsonRpcRequestContext;
import samba.jsonrpc.reponse.JsonRpcRequestId;
import samba.jsonrpc.reponse.JsonRpcResponse;

public interface JsonRpcProcessor {

  JsonRpcResponse process(final JsonRpcRequestId id, final JsonRpcMethod method, final JsonRpcRequestContext request);
}
