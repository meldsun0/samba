package samba.domain.messages.response;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.MessageType;
import samba.domain.messages.PortalWireMessage;
import samba.schema.messages.ssz.containers.NodesContainer;

import java.util.Base64;
import java.util.List;

import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;

/*
 * Response message to FindNodes(0x02).
 */
public class Nodes implements PortalWireMessage {

  private final byte total = 1;
  private final List<String> enrs;

  public Nodes(List<String> enrs) {
    // TODO ensure that total size of all ENRs does not exceed MAX_DISCV5_PACKET_PAYLOAD_BYTES
    // and/or trim
    checkArgument(enrs.size() <= MAX_ENRS, "Number of ENRs exceeds limit");
    checkArgument(
        enrs.stream().allMatch(enr -> enr.length() <= MAX_CUSTOM_PAYLOAD_BYTES),
        "One or more ENRs exceed maximum payload size");
    checkArgument(
        enrs.stream().mapToInt(enr -> enr.getBytes().length).sum() <= MAX_CUSTOM_PAYLOAD_BYTES,
        " Maximum payload size exceeded");

    /* TODO Validate all these:
     * Individual ENR records MUST correspond to one of the requested distances.
     * It is invalid to return multiple ENR records for the same node_id.
     */
    this.enrs = enrs;
  }

  public static Nodes fromSSZBytes(Bytes sszbytes) {
    Bytes container = sszbytes.slice(1);
    NodesContainer nodesContainer = NodesContainer.decodePacket(container);
    int total = nodesContainer.getTotal();
    if (total > 1)
      throw new IllegalArgumentException("NODES: Total number of Nodes messages must be 1");
    return new Nodes(nodesContainer.getEnrs());
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.NODES;
  }

  public int getTotal() {
    return Bytes.of(total).toInt();
  }

  public List<String> getEnrList() {
    return enrs;
  }

  private List<Bytes> getEnrsBytes() {
    return enrs.stream().map(enr -> Bytes.wrap(Base64.getUrlDecoder().decode(enr))).toList();
  }

  @Override
  public Bytes getSszBytes() {
    return Bytes.concatenate(
        SszByte.of(getMessageType().getByteValue()).sszSerialize(),
        new NodesContainer(total, getEnrsBytes()).sszSerialize());
  }

  @Override
  public Nodes getMessage() {
    return this;
  }

  public boolean isNodeListEmpty() {
    return this.enrs.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Nodes that = (Nodes) o;
    return Objects.equal(getEnrList(), that.getEnrList())
        && Objects.equal(getTotal(), that.getTotal());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getEnrList(), getTotal());
  }

  public List<String> getEnrsWithENRPerItem() {
    return this.getEnrList().stream().map(item -> "enr:" + item.replace("=", "")).toList();
  }

  public String toString() {
    return "Nodes{ total=" + total + ", enrs=" + enrs + '}';
  }
}
