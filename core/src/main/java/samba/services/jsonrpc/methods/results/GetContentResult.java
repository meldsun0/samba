package samba.services.jsonrpc.methods.results;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.tuweni.bytes.Bytes;

@JsonPropertyOrder({"content", "utpTransfer"})
public class GetContentResult {

  private final Bytes content;
  private final boolean utpTransfer;

  public GetContentResult(Bytes content, boolean utpTransfer) {
    this.content = content;
    this.utpTransfer = utpTransfer;
  }

  @JsonGetter(value = "content")
  public Bytes getContent() {
    return content;
  }

  @JsonGetter(value = "utpTransfer")
  public boolean getUtpTransfer() {
    return utpTransfer;
  }
}
