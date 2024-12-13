package samba.services.jsonrpc.methods.results;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"enrSeq", "dataRadius"})
public class PingResult {

  private final BigInteger enrSeq;
  private final String dataRadius;

  public PingResult(BigInteger enrSq, String dataRadius) {
    this.enrSeq = enrSq;
    this.dataRadius = dataRadius;
  }

  @JsonGetter(value = "enrSeq")
  public BigInteger getEnrSeq() {
    return enrSeq;
  }

  @JsonGetter(value = "dataRadius")
  public String getDataRadius() {
    return dataRadius;
  }
}
