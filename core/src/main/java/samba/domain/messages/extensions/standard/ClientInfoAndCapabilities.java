package samba.domain.messages.extensions.standard;

import static com.google.common.base.Preconditions.checkArgument;

import samba.config.VersionProvider;
import samba.domain.messages.extensions.ExtensionType;
import samba.domain.messages.extensions.PortalExtension;
import samba.domain.types.unsigned.UInt16;
import samba.schema.messages.ssz.containers.extensions.standard.ClientInfoAndCapabilitiesContainer;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;

public class ClientInfoAndCapabilities implements PortalExtension {

  private final String clientInfo;
  private final UInt256 dataRadius;
  private final List<UInt16> capabilities;

  public ClientInfoAndCapabilities(
      String clientInfo, UInt256 dataRadius, List<UInt16> capabilities) {
    checkArgument(
        clientInfo.length() <= MAX_CLIENT_INFO_BYTE_LENGTH,
        "Client info length exceeds maximum length");
    checkArgument(dataRadius != null, "Data radius cannot be null");
    checkArgument(
        capabilities.size() <= MAX_CAPABILITIES_LENGTH,
        "Capabilities length exceeds maximum length");
    this.clientInfo = clientInfo;
    this.dataRadius = dataRadius;
    this.capabilities = capabilities;
  }

  public ClientInfoAndCapabilities(String clientInfo, UInt256 dataRadius) {
    this(clientInfo, dataRadius, List.of(UInt16.ZERO, UInt16.MAX_VALUE));
  }

  public ClientInfoAndCapabilities(UInt256 dataRadius) {
    this(VersionProvider.VERSION, dataRadius);
  }

  public static ClientInfoAndCapabilities fromSszBytes(Bytes sszBytes) {
    ClientInfoAndCapabilitiesContainer container =
        ClientInfoAndCapabilitiesContainer.decode(sszBytes);
    return new ClientInfoAndCapabilities(
        container.getClientInfo(), container.getDataRadius(), container.getCapabilities());
  }

  public static UInt256 getDataRadiusFromSszBytes(Bytes sszBytes) {
    ClientInfoAndCapabilitiesContainer container =
        ClientInfoAndCapabilitiesContainer.decode(sszBytes);
    return container.getDataRadius();
  }

  @Override
  public ExtensionType ExtensionType() {
    return ExtensionType.CLIENT_INFO_AND_CAPABILITIES;
  }

  @Override
  public ClientInfoAndCapabilities getExtension() {
    return this;
  }

  public String getClientInfo() {
    return clientInfo;
  }

  public UInt256 getDataRadius() {
    return dataRadius;
  }

  public List<UInt16> getCapabilities() {
    return capabilities;
  }

  @Override
  public Bytes getSszBytes() {
    return new ClientInfoAndCapabilitiesContainer(clientInfo, dataRadius, capabilities)
        .sszSerialize();
  }
}
