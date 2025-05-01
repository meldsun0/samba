package samba.api.jsonrpc.schemas;

import samba.api.jsonrpc.schemas.serialization.UInt256JsonDeserializer;
import samba.api.jsonrpc.schemas.serialization.UInt256JsonSerializer;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.tuweni.units.bigints.UInt256;

@JsonPropertyOrder({"enr", "distance"})
public class TraceResultMetadataObjectJson {

  private String enr;

  @JsonDeserialize(using = UInt256JsonDeserializer.class)
  @JsonSerialize(using = UInt256JsonSerializer.class)
  private UInt256 distance;

  public TraceResultMetadataObjectJson(final String enr, final UInt256 distance) {
    this.enr = enr;
    this.distance = distance;
  }

  public TraceResultMetadataObjectJson() {}

  @JsonGetter(value = "enr")
  public String getEnr() {
    return enr;
  }

  @JsonGetter(value = "distance")
  public UInt256 getDistance() {
    return distance;
  }
}
