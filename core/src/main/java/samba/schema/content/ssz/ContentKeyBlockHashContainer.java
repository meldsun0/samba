package samba.schema.content.ssz;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszBytes32;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class  ContentKeyBlockHashContainer
    extends Container1<ContentKeyBlockHashContainer, SszBytes32> {

  public ContentKeyBlockHashContainer(Bytes32 blockHash) {
    super(ContentKeyBlockHashContainerSchema.INSTANCE, SszBytes32.of(blockHash));
  }

  public ContentKeyBlockHashContainer(TreeNode backingNode) {
    super(ContentKeyBlockHashContainerSchema.INSTANCE, backingNode);
  }

  public Bytes32 getBlockHash() {
    return getField0().get();
  }

  public static ContentKeyBlockHashContainerSchema createSchema() {
    return new ContentKeyBlockHashContainerSchema();
  }

  public static ContentKeyBlockHashContainer decodeBytes(Bytes sszBytes) {
    return ContentKeyBlockHashContainerSchema.INSTANCE.sszDeserialize(sszBytes);
  }

  public static class ContentKeyBlockHashContainerSchema
      extends ContainerSchema1<ContentKeyBlockHashContainer, SszBytes32> {
    public static final ContentKeyBlockHashContainerSchema INSTANCE =
        new ContentKeyBlockHashContainerSchema();

    private ContentKeyBlockHashContainerSchema() {
      super(SszPrimitiveSchemas.BYTES32_SCHEMA);
    }

    @Override
    public ContentKeyBlockHashContainer createFromBackingNode(TreeNode node) {
      return new ContentKeyBlockHashContainer(node);
    }
  }
}
