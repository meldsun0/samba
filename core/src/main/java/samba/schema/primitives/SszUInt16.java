package samba.schema.primitives;

import samba.domain.types.unsigned.UInt16;

import tech.pegasys.teku.infrastructure.json.types.DeserializableTypeDefinition;
import tech.pegasys.teku.infrastructure.ssz.SszData;
import tech.pegasys.teku.infrastructure.ssz.impl.AbstractSszPrimitive;
import tech.pegasys.teku.infrastructure.ssz.schema.impl.AbstractSszPrimitiveSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.json.SszTypeDefinitionWrapper;
import tech.pegasys.teku.infrastructure.ssz.tree.LeafDataNode;
import tech.pegasys.teku.infrastructure.ssz.tree.LeafNode;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

// TODO move to Teku
public class SszUInt16 extends AbstractSszPrimitive<UInt16> {

  public static final SszUInt16 ZERO = new SszUInt16(UInt16.ZERO);
  public static final SszUInt16 MAX_VALUE = new SszUInt16(UInt16.MAX_VALUE);

  public static SszUInt16 of(UInt16 value) {
    return new SszUInt16(value);
  }

  private SszUInt16(UInt16 value) {
    super(value, UINT16_SCHEMA);
  }

  public int getValue() {
    return get().getValue();
  }

  public static final AbstractSszPrimitiveSchema<UInt16, SszUInt16> UINT16_SCHEMA =
      new AbstractSszPrimitiveSchema<>(16) {
        @Override
        public UInt16 createFromLeafBackingNode(final LeafDataNode node, final int internalIndex) {
          return UInt16.fromBytes(node.getData().reverse());
        }

        @Override
        public TreeNode updateBackingNode(
            final TreeNode srcNode, final int index, final SszData newValue) {
          return LeafNode.create(((SszUInt16) newValue).get().toBytes().reverse());
        }

        @Override
        public SszUInt16 boxed(final UInt16 rawValue) {
          return SszUInt16.of(rawValue);
        }

        @Override
        public TreeNode getDefaultTree() {
          return LeafNode.ZERO_LEAVES[2];
        }

        @Override
        public DeserializableTypeDefinition<SszUInt16> getJsonTypeDefinition() {
          return SSZ_UINT16_TYPE_DEFINITION;
        }

        @Override
        public String toString() {
          return "UInt16";
        }
      };

  public static final DeserializableTypeDefinition<UInt16> UINT16_TYPE =
      DeserializableTypeDefinition.string(UInt16.class)
          .formatter(UInt16::toString)
          .parser(value -> UInt16.valueOf(Integer.parseInt(value)))
          .example("1")
          .description("unsigned 16-bit integer")
          .format("uint16")
          .build();

  public static final DeserializableTypeDefinition<SszUInt16> SSZ_UINT16_TYPE_DEFINITION =
      new SszTypeDefinitionWrapper<>(UINT16_SCHEMA, UINT16_TYPE);
}
