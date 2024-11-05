package samba.schema.ssz.containers;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.PortalWireMessage;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container1;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema1;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class FindNodesContainer extends Container1<FindNodesContainer, SszByteList> {

    public FindNodesContainer(Bytes list) {
        super(FindNodesSchema.INSTANCE, SszByteListSchema.create(PortalWireMessage.MAX_DISTANCES * 2).fromBytes(list));
    }

    public FindNodesContainer(TreeNode backingNode) {
        super(FindNodesSchema.INSTANCE, backingNode);
    }

    // Distances in inclusive range [0, 256]
    public Set<Integer> getDistances() {
        Bytes distancesBytes = getField0().getBytes();
        if (distancesBytes.size() % 2 != 0) {
            throw new IllegalArgumentException("Invalid distance bytes length");
        }
        Set<Integer> distances = new HashSet<>();
        for (int i = 0; i < distancesBytes.size(); i += 2) {
            distances.add(Bytes.wrap(distancesBytes.slice(i, 2).toArray(ByteOrder.LITTLE_ENDIAN)).toInt());
        }
        return distances;
    }

    public static FindNodesContainer decodePacket(Bytes packet) {
        FindNodesSchema schema = FindNodesSchema.INSTANCE;
        FindNodesContainer decodedPacket = schema.sszDeserialize(packet);
        return decodedPacket;
    }

    public static class FindNodesSchema extends ContainerSchema1<FindNodesContainer, SszByteList> {

        public static final FindNodesSchema INSTANCE = new FindNodesSchema();

        private FindNodesSchema() {
            super(SszByteListSchema.create(PortalWireMessage.MAX_DISTANCES * 2));
        }

        @Override
        public FindNodesContainer createFromBackingNode(TreeNode node) {
            return new FindNodesContainer(node);
        }
    }
}