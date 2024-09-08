package samba.domain.messages;

import java.util.Arrays;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Response message to FindNodes(0x02).
 */
public class Nodes implements PortalWireMessage {

    private static final int MAX_ENRS = 32;
    private final UInt64 enrSeq;
    private final byte total = 1;
    //
    private final Byte[][] enrs;

    public Nodes(UInt64 enrSeq, Byte[][] enrs) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(enrs.length <= MAX_ENRS, "Number of ENRs exceeds limit");
        checkArgument(Arrays.stream(enrs).allMatch(enr -> enr.length <= MAX_CUSTOM_PAYLOAD_SIZE), "One or more ENRs exceed maximum payload size");
        /* *
        * Individual ENR records MUST correspond to one of the requested distances.
           It is invalid to return multiple ENR records for the same node_id.
            The ENR record of the requesting node SHOULD be filtered out of the list.
        *
        * * */
        this.enrSeq = enrSeq;
        this.enrs = enrs;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.NODES;
    }

    public Byte[][] getEnrArray() {
          return enrs;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}
