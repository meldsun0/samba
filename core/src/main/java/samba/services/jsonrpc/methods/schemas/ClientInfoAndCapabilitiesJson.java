package samba.services.jsonrpc.methods.schemas;

import samba.domain.types.unsigned.UInt16;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.tuweni.units.bigints.UInt256;

@JsonPropertyOrder({"clientInfo", "dataRadius", "capabilities"})
public class ClientInfoAndCapabilitiesJson {
  private String clientInfo;

  @JsonDeserialize(using = UInt256JsonDeserializer.class)
  @JsonSerialize(using = UInt256JsonSerializer.class)
  private UInt256 dataRadius;

  @JsonDeserialize(contentUsing = UInt16JsonDeserializer.class)
  @JsonSerialize(contentUsing = UInt16JsonSerializer.class)
  private List<UInt16> capabilities;

  public ClientInfoAndCapabilitiesJson(
      String clientInfo, UInt256 dataRadius, List<UInt16> capabilities) {
    this.clientInfo = clientInfo;
    this.dataRadius = dataRadius;
    this.capabilities = capabilities;
  }

  public ClientInfoAndCapabilitiesJson() {}

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
