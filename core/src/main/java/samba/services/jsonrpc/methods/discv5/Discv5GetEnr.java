package samba.services.jsonrpc.methods.discv5;

import org.apache.tuweni.units.bigints.UInt256;
import samba.services.discovery.Discv5Client;
import samba.jsonrpc.config.RpcMethod;

import samba.services.jsonrpc.methods.parameters.InputsValidations;
import samba.jsonrpc.reponse.*;

public class Discv5GetEnr implements JsonRpcMethod {

    private final Discv5Client discv5Client;

    public Discv5GetEnr(Discv5Client discv5Client) {
        this.discv5Client = discv5Client;
    }

    @Override
    public String getName() {
        return RpcMethod.DISCV5_GET_ENR.getMethodName();
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext requestContext) {
        try {
            String nodeId = requestContext.getRequiredParameter(0, String.class);
            this.validateInput(nodeId);
            if ( nodeId .startsWith("0x")) {
                nodeId = nodeId.substring(2);
            }
            String enr = this.discv5Client.lookupEnr(UInt256.fromHexString(nodeId)).orElse("");
            return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), enr);
        } catch (JsonRpcParameter.JsonRpcParameterException e) {
            return new JsonRpcErrorResponse(requestContext.getRequest().getId(), RpcErrorType.INVALID_REQUEST);
        }
    }

    private void validateInput(String nodeId) throws JsonRpcParameter.JsonRpcParameterException {
        if (!InputsValidations.isValidateNodeId(nodeId)) {
            throw new JsonRpcParameter.JsonRpcParameterException(RpcErrorType.INVALID_REQUEST.toString());
        }
    }
}
