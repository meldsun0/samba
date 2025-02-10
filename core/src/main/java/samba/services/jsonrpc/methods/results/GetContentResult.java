package samba.services.jsonrpc.methods.results;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.tuweni.bytes.Bytes;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"content", "utpTransfer"})
public class GetContentResult {

  private final String content;
  private final boolean utpTransfer;

  public GetContentResult(String content, boolean utpTransfer) {
    this.content = content;
    this.utpTransfer = utpTransfer;
  }

  @JsonGetter(value = "content")
  public String getContent() {
    return content;
  }

  @JsonGetter(value = "utpTransfer")
  public boolean getUtpTransfer() {
    return utpTransfer;
  }
}
