package samba.rpc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.plugin.services.rpc.PluginRpcRequest;
import samba.BesuSambaPlugin;
import samba.SambaSDK;

public class GetBlockHeaderByBlockNumber implements PluginRpcMethod {

  private final CompletableFuture<SambaSDK> sambaSDKFuture;

  public GetBlockHeaderByBlockNumber(CompletableFuture<SambaSDK> sambaSDKFuture) {
    this.sambaSDKFuture = sambaSDKFuture;
  }

  @Override
  public String getNamespace() {
    return BesuSambaPlugin.PLUGIN_NAME;
  }

  @Override
  public String getName() {
    return RpcMethod.GET_BLOCK_HEADER_BY_BLOCK_NUMBER.getMethodName();
  }

  @Override
  public Object execute(PluginRpcRequest rpcRequest) {
    try {
      String blockNumber = new JsonRpcParameter().required(rpcRequest.getParams(), 0, String.class);
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getBlockHeaderByBlockNumber(blockNumber))
          .map(BlockHeader::toString)
          .orElse("");
    } catch (JsonRpcParameter.JsonRpcParameterException
        | ExecutionException
        | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
