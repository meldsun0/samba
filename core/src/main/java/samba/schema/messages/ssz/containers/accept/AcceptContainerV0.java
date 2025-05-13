package samba.schema.messages.ssz.containers.accept;

import samba.domain.messages.PortalWireMessage;

import java.util.BitSet;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.collections.SszBitlist;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteVector;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszBitlistSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteVectorSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class AcceptContainerV0 extends Container2<AcceptContainerV0, SszByteVector, SszBitlist> {

  public AcceptContainerV0(Bytes connectionId, Bytes contentKeysBitList) {
    super(
        AcceptSchema.INSTANCE,
        SszByteVector.fromBytes(connectionId),
        createSszBitlist(contentKeysBitList));
  }

  public AcceptContainerV0(TreeNode backingNode) {
    super(AcceptSchema.INSTANCE, backingNode);
  }

  private static SszBitlist createSszBitlist(Bytes contentKeysByteList) {
    int size = contentKeysByteList.size();
    BitSet bitSet = new BitSet(size);
    for (int i = 0; i < size; i++) {
      byte b = contentKeysByteList.get(i);
      if (b == 1) {
        bitSet.set(i);
      } else if (b != 0) {
        throw new IllegalArgumentException("Each byte must be either 0x00 or 0x01");
      }
    }
    SszBitlistSchema bitListSchema = SszBitlistSchema.create(PortalWireMessage.MAX_KEYS);
    return bitListSchema.wrapBitSet(size, bitSet);
  }

  private static Bytes booleanListToByteList(List<Boolean> booleanList) {
    byte[] byteArray = new byte[booleanList.size()];
    for (int i = 0; i < booleanList.size(); i++) {
      byteArray[i] = (byte) (booleanList.get(i) ? 1 : 0);
    }
    return Bytes.wrap(byteArray);
  }

  public int getConnectionId() {
    return getField0().getBytes().toInt();
  }

  public Bytes getContentKeysBitList() {
    return booleanListToByteList(getField1().asListUnboxed());
  }

  public static AcceptContainerV0 decodePacket(Bytes packet) {
    AcceptSchema schema = AcceptSchema.INSTANCE;
    AcceptContainerV0 decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class AcceptSchema
      extends ContainerSchema2<AcceptContainerV0, SszByteVector, SszBitlist> {

    public static final AcceptSchema INSTANCE = new AcceptSchema();

    private AcceptSchema() {
      super(SszByteVectorSchema.create(2), SszBitlistSchema.create(PortalWireMessage.MAX_KEYS));
    }

    @Override
    public AcceptContainerV0 createFromBackingNode(TreeNode node) {
      return new AcceptContainerV0(node);
    }
  }
}
