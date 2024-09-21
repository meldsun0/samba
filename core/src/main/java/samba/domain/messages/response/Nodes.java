package samba.domain.messages.response;

import org.apache.tuweni.units.bigints.UInt64;
import samba.domain.messages.MessageType;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to FindNodes(0x02).
 */
public class Nodes {

    private final UInt64 enrSeq;
    private final Byte total = 1;
    //
    private final Byte[][] enrs;

    public Nodes(UInt64 enrSeq, Byte[][] enrs) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        /* *
        * Individual ENR records MUST correspond to one of the requested distances.
           It is invalid to return multiple ENR records for the same node_id.
            The ENR record of the requesting node SHOULD be filtered out of the list.
        *
        * * */
        this.enrSeq = enrSeq;
        this.enrs = enrs;
    }

    public MessageType getMessageType() {
        return MessageType.NODES;
    }
    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}
