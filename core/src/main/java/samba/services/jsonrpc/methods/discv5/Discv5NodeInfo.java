package samba.services.jsonrpc.methods.discv5;

import org.ethereum.beacon.discovery.schema.NodeRecord;
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
        NodeRecord nodeRecord = this.discv5Client.getHomeNodeRecord();
        NodeInfo nodeInfo = new NodeInfo ( nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
        return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), nodeInfo);
    }

}
