package samba.rpc;

import java.util.Collection;
import java.util.HashSet;

public enum RpcMethod {
  GET_VERSION("getVersion"),
  GET_BLOCK_BODY_BY_BLOCK_HASH("getBlockBodyByBlockHash"),
  GET_BLOCK_HEADER_BY_BLOCK_HASH("getBlockHeaderByBlockHash"),
  GET_TRANSACTION_RECEIPT_BY_BLOCK_HASH("getBlockHeaderByBlockHash"),
  GET_BLOCK_HEADER_BY_BLOCK_NUMBER("getBlockHeaderByBlockNumber");

  private final String methodName;

  private static final Collection<String> allMethodNames;

  public String getMethodName() {
    return methodName;
  }

  static {
    allMethodNames = new HashSet<>();
    for (RpcMethod m : RpcMethod.values()) {
      allMethodNames.add(m.getMethodName());
    }
  }

  RpcMethod(final String methodName) {
    this.methodName = methodName;
  }

  public static boolean rpcMethodExists(final String rpcMethodName) {
    return allMethodNames.contains(rpcMethodName);
  }
}
