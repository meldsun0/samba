package rpc;

import org.hyperledger.besu.plugin.services.rpc.PluginRpcRequest;

public interface PluginRpcMethod {

  String getNamespace();

  String getName();

  Object execute(PluginRpcRequest rpcRequest);
}
