package samba.services.jsonrpc.methods.results;

import samba.domain.types.unsigned.UInt16;
import samba.services.jsonrpc.methods.schemas.UInt16JsonDeserializer;
import samba.services.jsonrpc.methods.schemas.UInt16JsonSerializer;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonPropertyOrder({"enrSeq", "payloadType", "payload"})
public class PingResult {

  private final BigInteger enrSeq;

  @JsonDeserialize(using = UInt16JsonDeserializer.class)
  @JsonSerialize(using = UInt16JsonSerializer.class)
  private final UInt16 payloadType;

  private final Object payload;

  public PingResult(BigInteger enrSq, UInt16 payloadType, Object payload) {
    this.enrSeq = enrSq;
    this.payloadType = payloadType;
    this.payload = payload;
  }

  @JsonGetter(value = "enrSeq")
  public BigInteger getEnrSeq() {
    return enrSeq;
  }

  @JsonGetter(value = "payloadType")
  public UInt16 getPayloadType() {
    return payloadType;
  }

  @JsonGetter(value = "payload")
  public Object getPayload() {
    return payload;
  }
}
