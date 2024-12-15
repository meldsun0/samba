package samba.services.jsonrpc.methods.discv5;

import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.reponse.*;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.methods.results.NodeInfo;

import org.ethereum.beacon.discovery.schema.NodeRecord;

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
        if (nodeRecord != null) {
            NodeInfo nodeInfo = new NodeInfo(nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
            return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), nodeInfo);
        } else {
            return new JsonRpcErrorResponse(requestContext.getRequest().getId(), RpcErrorType.INTERNAL_ERROR);
        }
    }
}
