package samba.domain.messages.response;

import java.util.Base64;
import java.util.List;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.ssz.containers.NodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/*
 * Response message to FindNodes(0x02).
 */
public class Nodes implements PortalWireMessage {

    private final byte total = 1;
    private final List<String> enrs;

    public Nodes(List<String> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.length() <= MAX_CUSTOM_PAYLOAD_BYTES), "One or more ENRs exceed maximum payload size");

        /*
         * Individual ENR records MUST correspond to one of the requested distances.
         * It is invalid to return multiple ENR records for the same node_id.
         * The ENR record of the requesting node SHOULD be filtered out of the list.
         */
        this.enrs = enrs;
    }

    public static Nodes fromSSZBytes(Bytes sszbytes, NodeRecord srcNode) {
        Bytes container = sszbytes.slice(1);
        NodesContainer nodesContainer = NodesContainer.decodePacket(container);
        int total = nodesContainer.getTotal();
        List<String> enrs = nodesContainer.getEnrs();

        if (total > 1) {
            throw new IllegalArgumentException("NODES: Total number of Nodes messages must be 1");
        }
        if (enrs.size() > PortalWireMessage.MAX_ENRS) {
            throw new IllegalArgumentException("NODES: Number of ENRs exceeds limit");
        }
        for (String enr : enrs) {
            if (enr.length() > PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES) {
                throw new IllegalArgumentException("NODES: One or more ENRs exceed maximum payload size");
            }
        }
        // TODO: Remove requesting node (this node) from the list of ENRs
        // TODO: It is invalid to return multiple ENR records for the same node_id
        return new Nodes(enrs);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.NODES;
    }

    public int getTotal() {
        return Bytes.of(total).toInt();
    }

    public List<String> getEnrList() {
        return enrs;
    }

    private List<Bytes> getEnrsBytes() {
        return enrs.stream().map(enr -> Bytes.wrap(Base64.getUrlDecoder().decode(enr))).toList();
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                new NodesContainer(total, getEnrsBytes()).sszSerialize());
    }

    @Override
    public Nodes getMessage() {
        return this;
    }

    public boolean isNodeListEmpty() {
        return this.enrs.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Nodes that = (Nodes) o;
        return Objects.equal(getEnrList(), that.getEnrList()) && Objects.equal(getTotal(), that.getTotal());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEnrList(), getTotal());
    }

    public String toString() {
        return "Nodes{ total=" + total + ", enrs=" + enrs + '}';
    }
}


