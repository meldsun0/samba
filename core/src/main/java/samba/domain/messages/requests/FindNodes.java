package samba.domain.messages.requests;

import java.util.List;
import java.util.Set;

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

    private final Set<Integer> distances;  //check each distance MUST unique

    public FindNodes(Set<Integer> distances) {
        checkArgument(!distances.isEmpty(), "Distances can not be 0");
        checkArgument(distances.size() <= MAX_DISTANCES, "Number of distances exceeds limit");
        checkEachDistance(distances);
        this.distances = distances;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_NODES;
    }

    public Set<Integer> getDistances() {
        return distances;
    }


    @Override
    public Bytes serialize() {
        //TODO FIX.
        return Bytes.EMPTY;
    }

    @Override
    public FindNodes getMessage() {
        return this;
    }

    private void checkEachDistance(Set<Integer> distances) {
        distances.forEach(distance -> checkArgument(distances.size() <= 256, "Distances greater than 256 are not allowed")
                );
    }
}
