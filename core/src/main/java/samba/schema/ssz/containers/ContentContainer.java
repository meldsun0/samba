package samba.schema.ssz.containers;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.messages.PortalWireMessage;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.SszUnion;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.containers.Container2;
import tech.pegasys.teku.infrastructure.ssz.containers.ContainerSchema2;
import tech.pegasys.teku.infrastructure.ssz.primitive.SszByte;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.infrastructure.ssz.schema.SszUnionSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteVectorSchema;
import tech.pegasys.teku.infrastructure.ssz.tree.TreeNode;

public class ContentContainer extends Container2<ContentContainer, SszByte, SszUnion> {

    private static final int CONTENT_KEY_BYTE_SIZE = 2;

    public ContentContainer(Byte contentType, Bytes content) {
        super(ContentSchema.INSTANCE, SszByte.of(contentType), getUnionValue(contentType.intValue(), content));
    }
    
    public ContentContainer(TreeNode backingNode) {
        super(ContentSchema.INSTANCE, backingNode);
    }

    private static SszUnion getUnionValue(int contentType, Bytes content) {
        switch (contentType) {
            case 0 -> {
                System.out.println("ContentContainer.getUnionValue: contentType = 0");
                //return ContentSchema.INSTANCE.getUnionSchema().createFromValue(contentType, ContentSchema.createByteVectorSchema(CONTENT_KEY_BYTE_SIZE).fromBytes(content.slice(0, 2)));
                return ContentSchema.INSTANCE.getUnionSchema().createFromValue(contentType, SszByte.of(content.get(0)));
            }
            case 1 -> {
                return ContentSchema.INSTANCE.getUnionSchema().createFromValue(contentType, ContentSchema.createByteListSchema(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE).fromBytes(content));
            }
            case 2 -> {
                return ContentSchema.INSTANCE.getUnionSchema().createFromValue(contentType, createSszBytesList(content));
            }
            default -> throw new AssertionError();
        }
    }

    private static SszList<SszByteList> createSszBytesList(Bytes enrs) {
        SszByteListSchema byteListSchema = SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE);
        SszListSchema<SszByteList, SszList<SszByteList>> listSchema = SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS);
        SszList<SszByteList> sszByteLists = listSchema.sszDeserialize(enrs);
        return sszByteLists;
    }

    public int getContentType() {
        return getField0().get().intValue();
    }

    public int getConnectionId() {
        if (getField1().getSelector() == 0) {
            SszByteList byteList = (SszByteList) getField1().getValue();
            return byteList.getBytes().toInt();
        }
        return -1;
    }

    public Bytes getContent() {
        if (getField1().getSelector() == 1) {
            SszByteList byteList = (SszByteList) getField1().getValue();
            return byteList.getBytes();
        }
        return Bytes.EMPTY;
    }

    public List<String> getEnrs() {
        if (getField1().getSelector() == 2) {
            SszList<SszByteList> sszByteLists = (SszList<SszByteList>) getField1().getValue();
            return sszByteLists.stream().map(sszByteList -> Base64.getUrlEncoder().encodeToString(sszByteList.getBytes().toArray())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public static ContentContainer decodePacket(Bytes packet) {
        ContentSchema schema = ContentSchema.INSTANCE;
        ContentContainer decodedPacket = schema.sszDeserialize(packet);
        System.out.println("ContentContainer.decodePacket: decodedPacket = " + decodedPacket);
        return decodedPacket;
    }

    public static class ContentSchema extends ContainerSchema2<ContentContainer, SszByte, SszUnion> {

        public static final ContentSchema INSTANCE = new ContentSchema();

        private ContentSchema() {
            super(SszPrimitiveSchemas.BYTE_SCHEMA, 
            SszUnionSchema.create(SszPrimitiveSchemas.BYTE_SCHEMA, createByteListSchema(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE), createByteListListSchema()));
        }

        private static SszByteVectorSchema createByteVectorSchema(int size) {
            return SszByteVectorSchema.create(size);
        }

        private static SszByteListSchema createByteListSchema(int maxSize) {
            return SszByteListSchema.create(maxSize);
        }

        private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
            SszByteListSchema byteListSchema = SszByteListSchema.create(PortalWireMessage.MAX_CUSTOM_PAYLOAD_SIZE);  
            return SszListSchema.create(byteListSchema, PortalWireMessage.MAX_ENRS);
        }

        @Override
        public ContentContainer createFromBackingNode(TreeNode node) {
            return new ContentContainer(node);
        }

        public SszUnionSchema getUnionSchema() {
            return (SszUnionSchema) getChildSchema(1);
        }
    }
}