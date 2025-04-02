package samba.services.jsonrpc.methods.schemas;

import samba.domain.types.unsigned.UInt16;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.tuweni.units.bigints.UInt256;

@JsonPropertyOrder({"clientInfo", "dataRadius", "capabilities"})
public class ClientInfoAndCapabilitiesJson {
  private final String clientInfo;

  @JsonDeserialize(using = UInt256Json.class)
  private final UInt256 dataRadius;

  @JsonDeserialize(contentUsing = UInt16Json.class)
  private final List<UInt16> capabilities;

  public ClientInfoAndCapabilitiesJson(
      String clientInfo, UInt256 dataRadius, List<UInt16> capabilities) {
    this.clientInfo = clientInfo;
    this.dataRadius = dataRadius;
    this.capabilities = capabilities;
  }

  @JsonGetter(value = "clientInfo")
  public String getClientInfo() {
    return clientInfo;
  }

  @JsonGetter(value = "dataRadius")
  public UInt256 getDataRadius() {
    return dataRadius;
  }

  @JsonGetter(value = "capabilities")
  public List<UInt16> getCapabilities() {
    return capabilities;
  }
}
