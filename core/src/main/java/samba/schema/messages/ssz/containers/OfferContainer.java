package samba.schema.messages.ssz.containers;

import samba.domain.messages.PortalWireMessage;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class OfferContainer extends Container1<OfferContainer, SszList<SszByteList>> {

  public OfferContainer(List<Bytes> contentKeys) {
    super(OfferSchema.INSTANCE, createSszBytesList(contentKeys));
  }

  public OfferContainer(TreeNode backingNode) {
    super(OfferSchema.INSTANCE, backingNode);
  }

  private static SszList<SszByteList> createSszBytesList(List<Bytes> contentKeys) {
    SszByteListSchema byteListSchema =
        SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
    List<SszByteList> sszByteLists =
        contentKeys.stream().map(byteListSchema::fromBytes).collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(byteListSchema, PortalWireMessage.MAX_KEYS)
            .createFromElements(sszByteLists);
  }

  public List<Bytes> getContentKeys() {
    return getField0().stream().map(SszByteList::getBytes).toList();
  }

  public static OfferContainer decodePacket(Bytes packet) {
    OfferSchema schema = OfferSchema.INSTANCE;
    OfferContainer decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class OfferSchema extends ContainerSchema1<OfferContainer, SszList<SszByteList>> {

    public static final OfferSchema INSTANCE = new OfferSchema();

    private OfferSchema() {
      super(createByteListListSchema());
    }

    private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
      SszByteListSchema byteListSchema =
          SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
      return SszListSchema.create(byteListSchema, PortalWireMessage.MAX_KEYS);
    }

    @Override
    public OfferContainer createFromBackingNode(TreeNode node) {
      return new OfferContainer(node);
    }
  }
}
