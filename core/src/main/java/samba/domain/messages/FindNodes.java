package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt32;
import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to get ENR records from the recipient's routing table at
 * the given logarithmic distances. The distance of 0 indicates a request for the recipient's own ENR record.
 */
public class FindNodes {

    private final UInt64 enrSeq;

    private final UInt32[] distances;

    public FindNodes(UInt64 enrSeq, UInt32[] distances) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        //check each distance MUST be within the inclusive range [0,256]
        //check each distance MUST unique
        this.enrSeq = enrSeq;
        this.distances = distances;
    }

    public MessageType getMessageType() {
        return MessageType.FIND_NODES;
    }
    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}
