package samba.schema.messages.ssz.containers.accept;

import samba.domain.messages.PortalWireMessage;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteVector;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteVectorSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class AcceptContainerV1 extends Container2<AcceptContainerV1, SszByteVector, SszByteList> {

  public AcceptContainerV1(Bytes connectionId, Bytes contentKeysBitList) {
    super(
        AcceptSchema.INSTANCE,
        SszByteVector.fromBytes(connectionId),
        SszByteListSchema.create(PortalWireMessage.MAX_KEYS).fromBytes(contentKeysBitList));
  }

  public AcceptContainerV1(TreeNode backingNode) {
    super(AcceptSchema.INSTANCE, backingNode);
  }

  public int getConnectionId() {
    return getField0().getBytes().toInt();
  }

  public Bytes getContentKeysByteList() {
    return getField1().getBytes();
  }

  public static AcceptContainerV1 decodePacket(Bytes packet) {
    AcceptSchema schema = AcceptSchema.INSTANCE;
    AcceptContainerV1 decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class AcceptSchema
      extends ContainerSchema2<AcceptContainerV1, SszByteVector, SszByteList> {

    public static final AcceptSchema INSTANCE = new AcceptSchema();

    private AcceptSchema() {
      super(SszByteVectorSchema.create(2), SszByteListSchema.create(PortalWireMessage.MAX_KEYS));
    }

    @Override
    public AcceptContainerV1 createFromBackingNode(TreeNode node) {
      return new AcceptContainerV1(node);
    }
  }
}
