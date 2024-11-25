package samba.services.jsonrpc.methods.discv5;

import samba.network.history.HistoryNetwork;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.config.RpcMethod;
import samba.services.jsonrpc.methods.results.NodeInfo;
import samba.services.jsonrpc.reponse.JsonRpcMethod;
import samba.services.jsonrpc.reponse.JsonRpcRequestContext;
import samba.services.jsonrpc.reponse.JsonRpcResponse;
import samba.services.jsonrpc.reponse.JsonRpcSuccessResponse;

import java.util.List;

public class Discv5NodeInfo implements JsonRpcMethod {

    private final Discv5Client discv5Client;

    public Discv5NodeInfo(Discv5Client discv5Client) {
        this.discv5Client = discv5Client;
    }

    @Override
    public String getName() {
        return RpcMethod.DISCV5_NODE_INFO.getMethodName();
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
         NodeInfo nodeInfo = new NodeInfo (discv5Client.getEnr().get(),
                 discv5Client.getNodeId().get().toHexString());
        return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), nodeInfo);
    }

}
