package samba.domain.messages.response;

import java.util.Arrays;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to FindNodes(0x02).
 */
public class Nodes implements PortalWireMessage {

    private final Bytes total = Bytes.ofUnsignedInt(1);
    //
    private final List<Bytes> enrs;

    public Nodes(List<Bytes> enrs) {
        checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(enrs.stream().allMatch(enr -> enr.size() <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
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

    public List<Bytes> getEnrList() {
          return enrs;
    }

    @Override
    public Bytes serialize() {
        Bytes totalSerialized = SSZ.encodeUInt8(total.toInt());
        Bytes enrsSerialized = SSZ.encodeBytesList(enrs);
        return Bytes.concatenate(
                SSZ.encodeUInt8(getMessageType().ordinal()),
                totalSerialized,
                enrsSerialized);
    }

    @Override
    public Nodes getMessage() {
        return this;
    }


}
