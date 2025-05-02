package samba.api.jsonrpc.results;

import samba.api.jsonrpc.schemas.TraceResultObjectJson;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"content", "utpTransfer", "trace"})
public class TraceGetContentResult {

  private String content;
  private boolean utpTransfer;
  private TraceResultObjectJson trace;

  public TraceGetContentResult(
      final String content, final boolean utpTransfer, final TraceResultObjectJson trace) {
    this.content = content;
    this.utpTransfer = utpTransfer;
    this.trace = trace;
  }

  public TraceGetContentResult() {}

  @JsonGetter(value = "content")
  public String getContent() {
    return content;
  }

  @JsonGetter(value = "utpTransfer")
  public boolean getUtpTransfer() {
    return utpTransfer;
  }

  @JsonGetter(value = "trace")
  public TraceResultObjectJson getTrace() {
    return trace;
  }
}
