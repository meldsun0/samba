package samba.schema.content.ssz;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ContentKeyBlockNumberContainer
    extends Container1<ContentKeyBlockNumberContainer, SszUInt64> {

  public ContentKeyBlockNumberContainer(UInt64 blockNumber) {
    super(ContentKeyBlockNumberContainerSchema.INSTANCE, SszUInt64.of(blockNumber));
  }

  public ContentKeyBlockNumberContainer(TreeNode backingNode) {
    super(ContentKeyBlockNumberContainerSchema.INSTANCE, backingNode);
  }

  public UInt64 getBlockNumber() {
    return getField0().get();
  }

  public static ContentKeyBlockNumberContainerSchema createSchema() {
    return new ContentKeyBlockNumberContainerSchema();
  }

  public static ContentKeyBlockNumberContainer decodeBytes(Bytes sszBytes) {
    return ContentKeyBlockNumberContainerSchema.INSTANCE.sszDeserialize(sszBytes);
  }

  public static class ContentKeyBlockNumberContainerSchema
      extends ContainerSchema1<ContentKeyBlockNumberContainer, SszUInt64> {
    public static final ContentKeyBlockNumberContainerSchema INSTANCE =
        new ContentKeyBlockNumberContainerSchema();

    private ContentKeyBlockNumberContainerSchema() {
      super(SszPrimitiveSchemas.UINT64_SCHEMA);
    }

    @Override
    public ContentKeyBlockNumberContainer createFromBackingNode(TreeNode node) {
      return new ContentKeyBlockNumberContainer(node);
    }
  }
}
