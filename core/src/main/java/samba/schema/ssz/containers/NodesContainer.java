package samba.schema.ssz.containers;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.PortalWireMessage;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class NodesContainer extends Container2<NodesContainer, SszByte, SszList<SszByteList>> {

    public NodesContainer(byte total, List<Bytes> enrs) {
        super(NodesSchema.INSTANCE, SszByte.of(total), createSszBytesList(enrs));
    }
    
    public NodesContainer(TreeNode backingNode) {
        super(NodesSchema.INSTANCE, backingNode);
    }

    private static SszList<SszByteList> createSszBytesList(List<Bytes> enrs) {
        SszByteListSchema byteListSchema = SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
        List<SszByteList> sszByteLists = enrs.stream()
            .map(byteListSchema::fromBytes)
            .collect(Collectors.toList());
        return (SszList<SszByteList>) SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS).createFromElements(sszByteLists);
    }

    public int getTotal() {
        return getField0().get();
    }

    public List<String> getEnrs() {
        return getField1().stream().map(sszByteList -> Base64.getUrlEncoder().encodeToString(sszByteList.getBytes().toArray())).collect(Collectors.toList());
    }

    public static NodesContainer decodePacket(Bytes packet) {
        NodesSchema schema = NodesSchema.INSTANCE;
        NodesContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }


    public static class NodesSchema extends ContainerSchema2<NodesContainer, SszByte, SszList<SszByteList>> {

        public static final NodesSchema INSTANCE = new NodesSchema();

        private NodesSchema() {

            super(SszPrimitiveSchemas.BYTE_SCHEMA, createByteListListSchema());
        }

        private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
            SszByteListSchema byteListSchema = SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
            return SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS);
        }

        @Override
        public NodesContainer createFromBackingNode(TreeNode node) {
            return new NodesContainer(node);
        }
    }
}