package samba.schema.messages.ssz.containers;

import samba.domain.messages.PortalWireMessage;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.SszUnion;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteVector;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszUnionSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteVectorSchema;

public class ContentContainer {

  private static final int CONTENT_KEY_BYTE_SIZE = 2;
  private static SszUnionSchema schema =
      SszUnionSchema.create(
          createByteVectorSchema(CONTENT_KEY_BYTE_SIZE),
          createByteListSchema(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES),
          createByteListListSchema());
  private final SszUnion union;

  public ContentContainer(Byte contentType, Bytes content) {
    this.union = getUnionValue(contentType.intValue(), content);
  }

  public ContentContainer(Byte contentType, List<Bytes> content) {
    this.union = schema.createFromValue(contentType.intValue(), createSszBytesList(content));
  }

  public ContentContainer(SszUnion union) {
    this.union = union;
  }

  private static SszUnion getUnionValue(int contentType, Bytes content) {
    switch (contentType) {
      case 0 -> {
        return schema.createFromValue(
            contentType,
            createByteVectorSchema(CONTENT_KEY_BYTE_SIZE).fromBytes(content.slice(0, 2)));
      }
      case 1 -> {
        return schema.createFromValue(
            contentType,
            createByteListSchema(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES).fromBytes(content));
      }
      default -> throw new AssertionError();
    }
  }

  private static SszByteVectorSchema createByteVectorSchema(int size) {
    return SszByteVectorSchema.create(size);
  }

  private static SszByteListSchema createByteListSchema(int maxSize) {
    return SszByteListSchema.create(maxSize);
  }

  private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
    SszByteListSchema byteListSchema =
        SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
    return SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS);
  }

  private static SszList<SszByteList> createSszBytesList(List<Bytes> enrs) {
    SszByteListSchema byteListSchema =
        SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_BYTES);
    List<SszByteList> sszByteLists =
        enrs.stream().map(byteListSchema::fromBytes).collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS)
            .createFromElements(sszByteLists);
  }

  public int getContentType() {
    return union.getSelector();
  }

  public int getConnectionId() {
    if (union.getSelector() == 0) {
      SszByteVector byteVector = (SszByteVector) union.getValue();
      return byteVector.getBytes().toInt();
    }
    return -1;
  }

  public Bytes getContent() {
    if (union.getSelector() == 1) {
      SszByteList byteList = (SszByteList) union.getValue();
      return byteList.getBytes();
    }
    return Bytes.EMPTY;
  }

  public List<String> getEnrs() {
    if (union.getSelector() == 2) {
      SszList<SszByteList> sszByteLists = (SszList<SszByteList>) union.getValue();
      return sszByteLists.stream()
          .map(
              sszByteList ->
                  Base64.getUrlEncoder().encodeToString(sszByteList.getBytes().toArray()))
          .collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  public static ContentContainer decodePacket(Bytes packet) {
    SszUnion decodedPacket = (SszUnion) schema.sszDeserialize(packet);
    return new ContentContainer(decodedPacket);
  }

  public Bytes sszSerialize() {
    return union.sszSerialize();
  }
}
