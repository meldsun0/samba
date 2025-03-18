package samba.services.jsonrpc.methods.results;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"storedLocally", "peerCount"})
public class PutContentResult {

  private final boolean storedLocally;
  private final int peerCount;

  public PutContentResult(final boolean storedLocally, final int peerCount) {
    this.storedLocally = storedLocally;
    this.peerCount = peerCount;
  }

  @JsonGetter(value = "storedLocally")
  public boolean isStoredLocally() {
    return storedLocally;
  }

  @JsonGetter(value = "peerCount")
  public int getPeerCount() {
    return peerCount;
  }
}
