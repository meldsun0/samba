package samba.api.jsonrpc.parameters;

import samba.jsonrpc.reponse.JsonRpcParameter;
import samba.jsonrpc.reponse.JsonRpcRequestContext;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;

public class ParametersUtil {

  public static String getNodeId(final JsonRpcRequestContext requestContext, final int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    String nodeId = requestContext.getRequiredParameter(index, String.class);
    if (!InputsValidations.isNodeIdValid(nodeId))
      throw new JsonRpcParameter.JsonRpcParameterException(
          String.format("Invalid nodeId parameter at index %d", index));
    return nodeId.startsWith("0x") ? nodeId.substring(2) : nodeId;
  }

  public static Bytes getBytesFromHexString(
      final JsonRpcRequestContext requestContext, final int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    return Bytes.fromHexString(requestContext.getRequiredParameter(index, String.class));
  }

  public static Bytes getContentBytesFromHexString(
      final JsonRpcRequestContext requestContext, int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    String bytesHex = requestContext.getRequiredParameter(index, String.class);
    if ("0x".equals(bytesHex)) {
      return Bytes.of(0);
    }
    return getBytesFromHexString(requestContext, index);
  }

  public static Bytes getContentKeyBytesFromHexString(
      final JsonRpcRequestContext requestContext, int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    String bytesHex = requestContext.getRequiredParameter(index, String.class);
    if ("0x".equals(bytesHex) || bytesHex.isEmpty()) {
      throw new JsonRpcParameter.JsonRpcParameterException(
          String.format("Invalid contentKey parameter at index %d", index));
    }
    return getBytesFromHexString(requestContext, index);
  }

  public static String getEnr(JsonRpcRequestContext requestContext, int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    return requestContext.getRequiredParameter(index, String.class);
  }

  public static Set<Integer> getDistances(JsonRpcRequestContext requestContext, int index)
      throws JsonRpcParameter.JsonRpcParameterException {
    return Arrays.stream(requestContext.getRequiredParameter(index, Integer[].class))
        .collect(Collectors.toSet());
  }

  public static InetSocketAddress getSocketAddress(
      JsonRpcRequestContext requestContext, int index) {
    return null;
  }
}
