package samba.api.jsonrpc.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"nodes"})
public class RecursiveFindNodesResult {

  private final List<String> nodes;

  public RecursiveFindNodesResult(final List<String> nodes) {
    if (nodes.size() > 16) {
      throw new IllegalArgumentException("The number of nodes must be less than or equal to 16.");
    }
    this.nodes = nodes;
  }

  @JsonGetter(value = "nodes")
  public List<String> getNodes() {
    return nodes;
  }
}
