package samba.api.jsonrpc.schemas;

import samba.api.jsonrpc.schemas.serialization.UInt256JsonDeserializer;
import samba.api.jsonrpc.schemas.serialization.UInt256JsonKeyDeserializer;
import samba.api.jsonrpc.schemas.serialization.UInt256JsonKeySerializer;
import samba.api.jsonrpc.schemas.serialization.UInt256JsonSerializer;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.tuweni.units.bigints.UInt256;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"origin", "targetId", "responses", "metadata", "startedAtMs", "cancelled"})
public class TraceResultObjectJson {

  @JsonDeserialize(using = UInt256JsonDeserializer.class)
  @JsonSerialize(using = UInt256JsonSerializer.class)
  private UInt256 origin;

  @JsonDeserialize(using = UInt256JsonDeserializer.class)
  @JsonSerialize(using = UInt256JsonSerializer.class)
  private UInt256 targetId;

  @JsonDeserialize(keyUsing = UInt256JsonKeyDeserializer.class)
  @JsonSerialize(keyUsing = UInt256JsonKeySerializer.class)
  private Map<UInt256, TraceResultResponseItemJson> responses;

  @JsonDeserialize(keyUsing = UInt256JsonKeyDeserializer.class)
  @JsonSerialize(keyUsing = UInt256JsonKeySerializer.class)
  private Map<UInt256, TraceResultMetadataObjectJson> metadata;

  private int startedAtMs;

  @JsonDeserialize(contentUsing = UInt256JsonDeserializer.class)
  @JsonSerialize(contentUsing = UInt256JsonSerializer.class)
  private List<UInt256> cancelled;

  public TraceResultObjectJson(
      final UInt256 origin,
      final UInt256 targetId,
      final Map<UInt256, TraceResultResponseItemJson> responses,
      final Map<UInt256, TraceResultMetadataObjectJson> metadata,
      final int startedAtMs,
      final List<UInt256> cancelled) {
    this.origin = origin;
    this.targetId = targetId;
    this.responses = responses;
    this.metadata = metadata;
    this.startedAtMs = startedAtMs;
    this.cancelled = cancelled;
  }

  public TraceResultObjectJson(
      final UInt256 origin,
      final UInt256 targetId,
      final Map<UInt256, TraceResultResponseItemJson> responses,
      final Map<UInt256, TraceResultMetadataObjectJson> metadata,
      final int startedAtMs) {
    this(origin, targetId, responses, metadata, startedAtMs, null);
  }

  public TraceResultObjectJson() {}

  @JsonGetter(value = "origin")
  public UInt256 getOrigin() {
    return origin;
  }

  @JsonGetter(value = "targetId")
  public UInt256 getTargetId() {
    return targetId;
  }

  @JsonGetter(value = "responses")
  public Map<UInt256, TraceResultResponseItemJson> getResponses() {
    return responses;
  }

  @JsonGetter(value = "metadata")
  public Map<UInt256, TraceResultMetadataObjectJson> getMetadata() {
    return metadata;
  }

  @JsonGetter(value = "startedAtMs")
  public int getStartedAtMs() {
    return startedAtMs;
  }

  @JsonGetter(value = "cancelled")
  public List<UInt256> getCancelled() {
    return cancelled;
  }
}
