package samba.domain.messages;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

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
}
