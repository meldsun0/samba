package samba.domain.messages.requests;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import samba.domain.messages.MessageType;
import static com.google.common.base.Preconditions.checkArgument;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
/**
 * Request message to get ENR records from the recipient's routing table at
 * the given logarithmic distances. The distance of 0 indicates a request for the recipient's own ENR record.
 */
public class FindNodes implements PortalWireMessage {

    private final List<Integer> distances;

    public FindNodes(List<Integer> distances) {
        checkArgument(distances.size() <= MAX_DISTANCES, "Number of distances exceeds limit");
        //check each distance MUST be within the inclusive range [0,256]
        //check each distance MUST unique
        this.distances = distances;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_NODES;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    @Override
    public Bytes serialize() {
        Bytes distancesSerialized = SSZ.encodeIntList(Integer.SIZE, distances);
        return Bytes.concatenate(
                SSZ.encodeUInt8(getMessageType().ordinal()),
                distancesSerialized);
        
    }

    @Override
    public FindNodes getMessage() {
        return this;
    }
}
