package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt32;
import org.apache.tuweni.units.bigints.UInt64;

import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Request message to get ENR records from the recipient's routing table at
 * the given logarithmic distances. The distance of 0 indicates a request for the recipient's own ENR record.
 */
public class FindNodes implements PortalWireMessage {

    private final static int MAX_DISTANCES = 256;

    private final UInt64 enrSeq;

    private final UInt32[] distances;

    public FindNodes(UInt64 enrSeq, UInt32[] distances) {
        checkArgument(enrSeq != null && UInt64.ZERO.compareTo(enrSeq) < 0, "enrSeq cannot be null or negative");
        checkArgument(distances.length <= MAX_DISTANCES, "Number of distances exceeds limit");
        //check each distance MUST be within the inclusive range [0,256]
        //check each distance MUST unique
        this.enrSeq = enrSeq;
        this.distances = distances;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_NODES;
    }

    public UInt32[] getDistances() {
        return distances;
    }

    public Optional<UInt64> getEnrSeq() {
        return Optional.ofNullable(enrSeq);
    }
}
