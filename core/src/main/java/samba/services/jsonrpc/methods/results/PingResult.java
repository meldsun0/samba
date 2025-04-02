package samba.services.jsonrpc.methods.results;

import samba.domain.types.unsigned.UInt16;
import samba.services.jsonrpc.methods.schemas.UInt16Json;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonPropertyOrder({"enrSeq", "payloadType", "payload"})
public class PingResult {

  private final BigInteger enrSeq;

  @JsonDeserialize(using = UInt16Json.class)
  private final UInt16 payloadType;

  private final String payload;

  public PingResult(BigInteger enrSq, UInt16 payloadType, String payload) {
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
  public String getPayload() {
    return payload;
  }
}
