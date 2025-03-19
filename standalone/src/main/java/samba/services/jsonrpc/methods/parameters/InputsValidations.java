package samba.services.jsonrpc.methods.parameters;

import java.util.regex.Pattern;

public class InputsValidations {

  private static final String NODE_ID_REGEX = "^0x[0-9a-f]{64}$";
  private static final String ENR_REGEX = "^enr:[a-zA-Z0-9_:-]{179}$";

  public static boolean isNodeIdValid(final String nodeId) {
    Pattern pattern = Pattern.compile(NODE_ID_REGEX);
    return pattern.matcher(nodeId).matches();
  }

  public static boolean isEnrValid(final String enr) {
    Pattern pattern = Pattern.compile(ENR_REGEX);
    return pattern.matcher(enr).matches();
  }
}
