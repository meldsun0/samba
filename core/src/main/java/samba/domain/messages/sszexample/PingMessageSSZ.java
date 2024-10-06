package samba.domain.messages.sszexample;







import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;


import tech.pegasys.teku.infrastructure.ssz.primitive.SszUInt64;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;


public class PingMessageSSZ extends Container2<PingMessageSSZ, SszUInt64, SszByteList> {

    public static class PingMessageSSZSchema extends ContainerSchema2<PingMessageSSZ, SszUInt64, SszByteList> {

        public PingMessageSSZSchema() {
            super(
                    "0x00",
                    namedSchema("enr_seq", SszPrimitiveSchemas.UINT64_SCHEMA),
                    namedSchema("custom_payload", SszByteListSchema.create(2048)));
        }

        @Override
        public PingMessageSSZ createFromBackingNode(final TreeNode node) {
            return new PingMessageSSZ(this, node);
        }

        public SszByteListSchema<?> getExtraDataSchema() {
            return (SszByteListSchema<?>) getChildSchema(getFieldIndex("custom_payload"));
        }
    }

    public static final PingMessageSSZSchema SSZ_SCHEMA = new PingMessageSSZSchema();

    private PingMessageSSZ(final PingMessageSSZSchema type, final TreeNode backingNode) {
        super(type, backingNode);
    }

    public PingMessageSSZ(final long enr_seq,  Bytes payload ) {
        super(SSZ_SCHEMA, SszUInt64.of(UInt64.valueOf(enr_seq)), SszByteListSchema.create(2048).fromBytes(payload));
    }

}
