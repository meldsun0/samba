package samba.api.jsonrpc.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"localNodeId", "buckets"})
public class RoutingTableInfoResult {

  private final String localNodeId;
  private final List<List<String>> buckets;

  public RoutingTableInfoResult(String localNodeId, List<List<String>> buckets) {
    this.localNodeId = localNodeId;
    this.buckets = buckets;
  }

  @JsonGetter(value = "localNodeId")
  public String getLocalNodeId() {
    return localNodeId;
  }

  @JsonGetter(value = "buckets")
  public List<List<String>> getBuckets() {
    return buckets;
  }
}
