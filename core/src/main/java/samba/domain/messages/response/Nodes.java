package samba.domain.messages.response;

import java.util.Base64;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.*;
import samba.schema.ssz.containers.NodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Response message to FindNodes(0x02).
 */
public class Nodes implements PortalWireMessage {

    private final byte total = 1;
    //
    private final List<String> enrs;

    public Nodes(List<String> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.length() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        /* *
        * Individual ENR records MUST correspond to one of the requested distances.
           It is invalid to return multiple ENR records for the same node_id.
            The ENR record of the requesting node SHOULD be filtered out of the list.
        *
        * * */
        this.enrs = enrs;
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
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new NodesContainer(total, getEnrsBytes()).sszSerialize());
    }
}
