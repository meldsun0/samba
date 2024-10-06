package samba.schema.ssz.containers;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.PortalWireMessage;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class FindContentContainer extends Container1<FindContentContainer, SszByteList> {

    public FindContentContainer(Bytes contentKey) {
        super(FindContentSchema.INSTANCE, SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE).fromBytes(contentKey));
    }

    public FindContentContainer(TreeNode backingNode) {
        super(FindContentSchema.INSTANCE, backingNode);
    }

    public Bytes getContentKey() {
        return getField0().getBytes();
    }

    public static FindContentContainer decodePacket(Bytes packet) {
        FindContentSchema schema = FindContentSchema.INSTANCE;
        FindContentContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }

    public static class FindContentSchema extends ContainerSchema1<FindContentContainer, SszByteList> {

        public static final FindContentSchema INSTANCE = new FindContentSchema();

        private FindContentSchema() {
            super(SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE));
        }

        @Override
        public FindContentContainer createFromBackingNode(TreeNode node) {
            return new FindContentContainer(node);
        }
    }
}