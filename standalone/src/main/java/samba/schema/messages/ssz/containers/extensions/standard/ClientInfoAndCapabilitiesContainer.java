package samba.schema.messages.ssz.containers.extensions.standard;

import samba.domain.messages.extensions.PortalExtension;
import samba.domain.types.unsigned.UInt16;
import samba.schema.primitives.SszUInt16;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container3;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema3;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt256;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class ClientInfoAndCapabilitiesContainer
    extends Container3<
        ClientInfoAndCapabilitiesContainer, SszByteList, SszUInt256, SszList<SszUInt16>> {

  public ClientInfoAndCapabilitiesContainer(
      String clientInfo, UInt256 dataRadius, List<UInt16> capabilities) {
    super(
        ClientInfoAndCapabilitiesSchema.INSTANCE,
        SszByteListSchema.create(PortalExtension.MAX_CLIENT_INFO_BYTE_LENGTH)
            .fromBytes(Bytes.wrap(clientInfo.getBytes(StandardCharsets.UTF_8))),
        SszUInt256.of(dataRadius),
        SszListSchema.create(SszUInt16.UINT16_SCHEMA, PortalExtension.MAX_CAPABILITIES_LENGTH)
            .createFromElements(createSszUInt16List(capabilities)));
  }

  public ClientInfoAndCapabilitiesContainer(TreeNode backingNode) {
    super(ClientInfoAndCapabilitiesSchema.INSTANCE, backingNode);
  }

  private static List<SszUInt16> createSszUInt16List(List<UInt16> capabilities) {
    return capabilities.stream().map(SszUInt16::of).collect(Collectors.toList());
  }

  public String getClientInfo() {
    return new String(getField0().getBytes().toArray(), StandardCharsets.UTF_8);
  }

  public UInt256 getDataRadius() {
    return getField1().get();
  }

  public List<UInt16> getCapabilities() {
    return getField2().stream().map(SszUInt16::get).collect(Collectors.toList());
  }

  public static ClientInfoAndCapabilitiesContainer decode(Bytes sszBytes) {
    ClientInfoAndCapabilitiesSchema schema = ClientInfoAndCapabilitiesSchema.INSTANCE;
    ClientInfoAndCapabilitiesContainer decodedBytes = schema.sszDeserialize(sszBytes);
    return decodedBytes;
  }

  public static class ClientInfoAndCapabilitiesSchema
      extends ContainerSchema3<
          ClientInfoAndCapabilitiesContainer, SszByteList, SszUInt256, SszList<SszUInt16>> {

    public static final ClientInfoAndCapabilitiesSchema INSTANCE =
        new ClientInfoAndCapabilitiesSchema();

    private ClientInfoAndCapabilitiesSchema() {
      super(
          SszByteListSchema.create(PortalExtension.MAX_CLIENT_INFO_BYTE_LENGTH),
          SszPrimitiveSchemas.UINT256_SCHEMA,
          createUInt16ListSchema());
    }

    @SuppressWarnings("unchecked")
    private static SszListSchema<SszUInt16, SszList<SszUInt16>> createUInt16ListSchema() {
      return (SszListSchema<SszUInt16, SszList<SszUInt16>>)
          SszListSchema.create(SszUInt16.UINT16_SCHEMA, PortalExtension.MAX_CAPABILITIES_LENGTH);
    }

    @Override
    public ClientInfoAndCapabilitiesContainer createFromBackingNode(TreeNode node) {
      return new ClientInfoAndCapabilitiesContainer(node);
    }
  }
}
