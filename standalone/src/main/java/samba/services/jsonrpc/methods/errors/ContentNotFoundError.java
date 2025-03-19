package samba.services.jsonrpc.methods.errors;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"ContentNotFoundError"})
public class ContentNotFoundError {

  private ContentNotFoundErrorParams contentNotFoundErrorParams;

  public ContentNotFoundError() {
    this.contentNotFoundErrorParams = new ContentNotFoundErrorParams();
  }

  @JsonGetter(value = "ContentNotFoundError")
  public ContentNotFoundErrorParams getContentNotFoundErrorParams() {
    return contentNotFoundErrorParams;
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonPropertyOrder({"code", "message"})
  public static class ContentNotFoundErrorParams {

    private int code = -39001;
    private String message = "Content not found";

    public ContentNotFoundErrorParams(int code, String message) {
      this.code = code;
      this.message = message;
    }

    public ContentNotFoundErrorParams() {}

    @JsonGetter(value = "code")
    public int getCode() {
      return code;
    }

    @JsonGetter(value = "message")
    public String getMessage() {
      return message;
    }
  }
}
