package samba.jsonrpc.exception;

import samba.jsonrpc.reponse.RpcErrorType;

public class InvalidJsonRpcParameters extends InvalidJsonRpcRequestException {

  public InvalidJsonRpcParameters(final String s) {
    super(s);
  }

  public InvalidJsonRpcParameters(final String message, final RpcErrorType rpcErrorType) {
    super(message, rpcErrorType);
  }

  public InvalidJsonRpcParameters(final String message, final Throwable cause) {
    super(message, cause);
  }

  public InvalidJsonRpcParameters(
      final String message, final RpcErrorType rpcErrorType, final Throwable cause) {
    super(message, rpcErrorType, cause);
  }
}
