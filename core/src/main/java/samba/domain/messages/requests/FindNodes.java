package samba.domain.messages.requests;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import io.vertx.core.buffer.Buffer;
import org.ethereum.beacon.discovery.message.FindNodeMessage;
import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.ssz.containers.FindNodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to get ENR records from the recipient's routing table at
 * the given logarithmic distances. The distance of 0 indicates a request for the recipient's own ENR record.
 */
public class FindNodes implements PortalWireMessage {

    private final List<Integer> distances;

    public FindNodes(List<Integer> distances) {
        //TODO heck each distance MUST unique
        checkArgument(!distances.isEmpty(), "Distances can not be 0");
        checkArgument(distances.size() <= MAX_DISTANCES, "Number of distances exceeds limit");
        checkEachDistance(distances);
        this.distances = distances;
    }

    public static FindNodes fromSSZBytes(Bytes sszbytes) {
        Bytes container = sszbytes.slice(1);
        FindNodesContainer findNodesContainer = FindNodesContainer.decodePacket(container);
        List<Integer> distances = findNodesContainer.getDistances();
        Set<Integer> uniqueDistances = new HashSet<>(distances);
        distances.clear();
        distances.addAll(uniqueDistances);

        if (distances.size() > PortalWireMessage.MAX_DISTANCES) {
            throw new IllegalArgumentException("FINDNODES: Number of distances exceeds limit");
        } else {
            for (Integer distance : distances) {
                if (distance < 0 || distance > 256) {
                    throw new IllegalArgumentException("FINDNODES: Distance must be within the inclusive range [0,256]");
                }
            }
        }
        return new FindNodes(distances);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FIND_NODES;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    private Bytes getDistancesBytes() {
        Buffer distancesBytesBuffer = Buffer.buffer();
        for (Integer distance : distances)
            Bytes.ofUnsignedShort(distance, ByteOrder.LITTLE_ENDIAN).appendTo(distancesBytesBuffer);
        return Bytes.wrapBuffer(distancesBytesBuffer);
    }

    @Override
    public Bytes getSszBytes() {
        return Bytes.concatenate(
                SszByte.of(getMessageType().getByteValue()).sszSerialize(),
                new FindNodesContainer(getDistancesBytes()).sszSerialize());
    }

    ;

    @Override
    public FindNodes getMessage() {
        return this;
    }

    private void checkEachDistance(List<Integer> distances) {
        distances.forEach(distance -> checkArgument(distances.size() <= 256, "Distances greater than 256 are not allowed"));
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FindNodes that = (FindNodes) o;
        return Objects.equal(getDistances(), that.getDistances());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDistances());
    }

    @Override
    public String toString() {
        return "FindNodes{ distances=" + getDistances() + '}';
    }

}
