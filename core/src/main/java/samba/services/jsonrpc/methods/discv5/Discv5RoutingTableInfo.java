package samba.services.jsonrpc.methods.discv5;

import samba.services.discovery.Discv5Client;
import samba.services.discovery.Discv5Service;
import samba.services.jsonrpc.config.RpcMethod;
import samba.services.jsonrpc.reponse.JsonRpcMethod;
import samba.services.jsonrpc.reponse.JsonRpcRequestContext;
import samba.services.jsonrpc.reponse.JsonRpcResponse;

//TODO kbucket table is not accesible through discv5 library.
public class Discv5RoutingTableInfo implements JsonRpcMethod {

    private final Discv5Client discv5Client;

    public Discv5RoutingTableInfo(Discv5Service discv5Client) {
        this.discv5Client =  discv5Client;
    }

    @Override
    public String getName() {
        return RpcMethod.DISCV5_ROUTING_TABLE_INFO.getMethodName();
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
        return null;
    }
}
