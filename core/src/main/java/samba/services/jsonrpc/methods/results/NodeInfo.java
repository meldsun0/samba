package samba.services.jsonrpc.methods.results;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"nodeENR", "nodeId"})
public class NodeInfo {

  private final String nodeENR;
  private final String nodeId;

  public NodeInfo(String nodeENR, String nodeId) {
    this.nodeENR = nodeENR;
    this.nodeId = nodeId;
  }

  @JsonGetter(value = "nodeENR")
  public String getNodeENR() {
    return nodeENR;
  }

  @JsonGetter(value = "nodeId")
  public String getNodeId() {
    return nodeId;
  }
}
