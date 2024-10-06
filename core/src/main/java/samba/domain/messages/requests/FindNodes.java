package samba.domain.messages.requests;

import java.nio.ByteOrder;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkArgument;

import io.vertx.core.buffer.Buffer;
import samba.domain.messages.*;
import samba.schema.ssz.containers.FindNodesContainer;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

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

    private Bytes getDistancesBytes() {
        Buffer distancesBytesBuffer = Buffer.buffer();
        for (Integer distance : distances) Bytes.ofUnsignedShort(distance, ByteOrder.LITTLE_ENDIAN).appendTo(distancesBytesBuffer);
        return Bytes.wrapBuffer(distancesBytesBuffer);
    }

    @Override
    public Bytes serialize() {
        return Bytes.concatenate(
            SszByte.of(getMessageType().getByteValue()).sszSerialize(), 
            new FindNodesContainer(getDistancesBytes()).sszSerialize());
    };
}
