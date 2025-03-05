package rpc;

import org.hyperledger.besu.plugin.services.rpc.PluginRpcRequest;

public class SambaVersionRPCMethod implements PluginRpcMethod {

  @Override
  public String getNamespace() {
    return "samba";
  }

  @Override
  public String getName() {
    return "getVersion";
  }

  @Override
  public Object execute(PluginRpcRequest rpcRequest) {
    return "1.0";
  }
}
