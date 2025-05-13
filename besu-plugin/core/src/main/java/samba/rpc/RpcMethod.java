package samba.rpc;

import java.util.Collection;
import java.util.HashSet;

public enum RpcMethod {
  GET_VERSION("getVersion"),
  GET_BLOCK_BODY_BY_HASH("getBlockBodyByBlockHash");

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
