package samba.domain.messages.requests;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.messages.ssz.containers.FindNodesContainer;

import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/**
 * Request message to get ENR records from the recipient's routing table at the given logarithmic
 * distances. The distance of 0 indicates a request for the recipient's own ENR record.
 */
public class FindNodes implements PortalWireMessage {

  private final Set<Integer> distances;

  public FindNodes(Set<Integer> distances) {
    checkArgument(!distances.isEmpty(), "Distances can not be 0");
    checkArgument(distances.size() <= MAX_DISTANCES, "Number of distances exceeds limit");
    checkArgument(
        distances.stream().allMatch(distance -> distance >= 0 && distance <= 256),
        "One or more ENRs exceed maximum payload size");
    this.distances = distances;
  }

  public static FindNodes fromSSZBytes(Bytes sszbytes) {
    Bytes container = sszbytes.slice(1);
    FindNodesContainer findNodesContainer = FindNodesContainer.decodePacket(container);
    return new FindNodes(new HashSet<>(findNodesContainer.getDistances()));
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.FIND_NODES;
  }

  public Set<Integer> getDistances() {
    return distances;
  }

  private Bytes getDistancesBytes() {
    return Bytes.concatenate(
        distances.stream()
            .map(distance -> Bytes.ofUnsignedShort(distance, ByteOrder.LITTLE_ENDIAN))
            .toArray(Bytes[]::new));
  }

  @Override
  public Bytes getSszBytes() {
    return Bytes.concatenate(
        SszByte.of(getMessageType().getByteValue()).sszSerialize(),
        new FindNodesContainer(getDistancesBytes()).sszSerialize());
  }

  @Override
  public FindNodes getMessage() {
    return this;
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
