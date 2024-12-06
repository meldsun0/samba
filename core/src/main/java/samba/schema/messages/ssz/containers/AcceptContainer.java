package samba.schema.messages.ssz.containers;

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

public class AcceptContainer extends Container2<AcceptContainer, SszByteVector, SszBitlist> {

  public AcceptContainer(Bytes connectionId, Bytes contentKeysBitList) {
    super(
        AcceptSchema.INSTANCE,
        SszByteVector.fromBytes(connectionId),
        createSszBitlist(contentKeysBitList));
  }

  public AcceptContainer(TreeNode backingNode) {
    super(AcceptSchema.INSTANCE, backingNode);
  }

  private static SszBitlist createSszBitlist(Bytes contentKeysBitList) {
    BitSet bitSet = BitSet.valueOf(contentKeysBitList.toArray());
    SszBitlistSchema bitListSchema = SszBitlistSchema.create(PortalWireMessage.MAX_KEYS);
    SszBitlist bitList = bitListSchema.wrapBitSet(contentKeysBitList.size() * 8, bitSet);
    return bitList;
  }

  private static Bytes booleanListToBytes(List<Boolean> booleanList) {
    int size = booleanList.size();
    int byteArrayLength = (size + 7) / 8;
    byte[] byteArray = new byte[byteArrayLength];

    for (int i = 0; i < size; i++) {
      if (booleanList.get(i)) {
        byteArray[i / 8] |= (1 << (i % 8));
      }
    }
    return Bytes.wrap(byteArray);
  }

  public int getConnectionId() {
    return getField0().getBytes().toInt();
  }

  public Bytes getContentKeysBitList() {
    return booleanListToBytes(getField1().asListUnboxed());
  }

  public static AcceptContainer decodePacket(Bytes packet) {
    AcceptSchema schema = AcceptSchema.INSTANCE;
    AcceptContainer decodedPacket = schema.sszDeserialize(packet);
    return decodedPacket;
  }

  public static class AcceptSchema
      extends ContainerSchema2<AcceptContainer, SszByteVector, SszBitlist> {

    public static final AcceptSchema INSTANCE = new AcceptSchema();

    private AcceptSchema() {
      super(SszByteVectorSchema.create(2), SszBitlistSchema.create(PortalWireMessage.MAX_KEYS));
    }

    @Override
    public AcceptContainer createFromBackingNode(TreeNode node) {
      return new AcceptContainer(node);
    }
  }
}
