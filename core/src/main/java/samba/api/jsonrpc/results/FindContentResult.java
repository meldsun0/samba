package samba.api.jsonrpc.results;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"content", "utpTransfer", "enrs"})
public class FindContentResult {

  private String content;
  private Boolean utpTransfer;
  private List<String> enrs;

  public FindContentResult(final String content, final Boolean utpTransfer) {
    this.content = content;
    this.utpTransfer = utpTransfer;
    this.enrs = null;
  }

  public FindContentResult(final List<String> enrs) {
    this.content = null;
    this.utpTransfer = null;
    this.enrs = enrs.stream().map(enr -> enr.replace("=", "")).toList();
  }

  public FindContentResult() {}

  @JsonGetter(value = "content")
  public String getContent() {
    return content;
  }

  @JsonGetter(value = "utpTransfer")
  public Boolean getUtpTransfer() {
    return utpTransfer;
  }

  @JsonGetter(value = "enrs")
  public List<String> getEnrs() {
    return enrs;
  }
}
