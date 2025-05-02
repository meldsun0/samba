package samba.api.jsonrpc.schemas;

import samba.api.jsonrpc.schemas.serialization.UInt256JsonDeserializer;
import samba.api.jsonrpc.schemas.serialization.UInt256JsonSerializer;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.tuweni.units.bigints.UInt256;

@JsonPropertyOrder({"durationMs", "respondedWith"})
public class TraceResultResponseItemJson {

  private long durationMs;

  @JsonDeserialize(contentUsing = UInt256JsonDeserializer.class)
  @JsonSerialize(contentUsing = UInt256JsonSerializer.class)
  private List<UInt256> respondedWith;

  public TraceResultResponseItemJson(final long durationMs, final List<UInt256> respondedWith) {
    if (durationMs < 0) {
      throw new IllegalArgumentException("durationMs must be non-negative");
    }
    this.durationMs = durationMs;
    this.respondedWith = respondedWith;
  }

  public TraceResultResponseItemJson() {}

  @JsonGetter(value = "durationMs")
  public long getDurationMs() {
    return durationMs;
  }

  @JsonGetter(value = "respondedWith")
  public List<UInt256> getRespondedWith() {
    return respondedWith;
  }
}
