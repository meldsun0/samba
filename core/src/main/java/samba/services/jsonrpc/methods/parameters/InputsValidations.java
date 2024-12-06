package samba.services.jsonrpc.methods.parameters;

import java.util.regex.Pattern;

public class InputsValidations {

  private static final String NODE_ID_REGEX = "^0x[0-9a-f]{64}$";

  public static boolean isValidateNodeId(String nodeId) {
    Pattern pattern = Pattern.compile(NODE_ID_REGEX);
    return pattern.matcher(nodeId).matches();
  }
}
