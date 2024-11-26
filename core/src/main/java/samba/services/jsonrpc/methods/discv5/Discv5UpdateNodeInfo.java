package samba.services.jsonrpc.methods.discv5;

import io.libp2p.core.multiformats.Multiaddr;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.services.discovery.Discv5Client;
import samba.services.jsonrpc.config.RpcMethod;
import samba.services.jsonrpc.methods.results.NodeInfo;
import samba.services.jsonrpc.reponse.*;
import java.util.Optional;

public class Discv5UpdateNodeInfo implements JsonRpcMethod {

    private final Discv5Client discv5Client;

    public Discv5UpdateNodeInfo(Discv5Client discv5Client) {
        this.discv5Client = discv5Client;
    }

    @Override
    public String getName() {
        return RpcMethod.DISCV5_UPDATE_NODE_INFO.getMethodName();
    }

    //TODO validate if the socketAddres is multiaddr .
    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
        try {
            final String socketAddr = requestContext.getRequiredParameter(0, String.class);
            final Optional<Boolean> isTCP = requestContext.getOptionalParameter(1, Boolean.class);  //TODO what to do with this ?

            Multiaddr multiaddr = Multiaddr.fromString(socketAddr);
            NodeRecord nodeRecord = this.discv5Client.updateNodeRecordSocket(multiaddr);
            NodeInfo nodeInfo = new NodeInfo(nodeRecord.asEnr(), nodeRecord.getNodeId().toHexString());
            return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), nodeInfo);
        } catch (JsonRpcParameter.JsonRpcParameterException e) {
            return new JsonRpcErrorResponse(requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
        }
    }

}