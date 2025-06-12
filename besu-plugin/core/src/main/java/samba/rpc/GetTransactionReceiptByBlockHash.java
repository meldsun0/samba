package samba.rpc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.api.jsonrpc.internal.parameters.JsonRpcParameter;
import org.hyperledger.besu.plugin.data.TransactionReceipt;
import org.hyperledger.besu.plugin.services.rpc.PluginRpcRequest;
import samba.BesuSambaPlugin;
import samba.SambaSDK;

public class GetTransactionReceiptByBlockHash implements PluginRpcMethod {

  private final CompletableFuture<SambaSDK> sambaSDKFuture;

  public GetTransactionReceiptByBlockHash(CompletableFuture<SambaSDK> sambaSDKFuture) {
    this.sambaSDKFuture = sambaSDKFuture;
  }

  @Override
  public String getNamespace() {
    return BesuSambaPlugin.PLUGIN_NAME;
  }

  @Override
  public String getName() {
    return RpcMethod.GET_TRANSACTION_RECEIPT_BY_BLOCK_HASH.getMethodName();
  }

  @Override
  public Object execute(PluginRpcRequest rpcRequest) {
    try {
      String rawParam = new JsonRpcParameter().required(rpcRequest.getParams(), 0, String.class);
      Hash blockHash = Hash.fromHexString(rawParam);
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getTransactionReceiptByBlockHash(blockHash))
          .map(
              transactionReceipts ->
                  transactionReceipts.stream()
                      .map(TransactionReceipt::toString)
                      .collect(Collectors.toList()))
          .orElse(Collections.emptyList());
    } catch (JsonRpcParameter.JsonRpcParameterException
        | ExecutionException
        | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
