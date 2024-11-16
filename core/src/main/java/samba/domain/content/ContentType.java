package samba.domain.content;

import org.apache.tuweni.bytes.Bytes;

import static com.google.common.base.Preconditions.checkNotNull;

public enum ContentType {

    BLOCK_HEADER(0x00),
    BLOCK_BODY(0x01),
    RECEIPT(0x02),
    BLOCK_HEADER_BY_NUMBER(0x03);

    private final byte value;
    private static final int BYTE_MASK = 0xFF;

    ContentType(int value) {
        this.value = (byte) (value & BYTE_MASK);
    }

    public byte getByteValue() {
        return value;
    }

    public static ContentType fromContentKey(Bytes contentKey) {
        checkNotNull(contentKey, "ContentKey is null");
        Bytes selector = contentKey.slice(0, 1);
        ContentType contentType = fromInt(selector.toInt());
        checkNotNull(contentType, "Invalid content type from byte: " + selector);
        return contentType;
    }

    private static ContentType fromInt(int value) {
        value = value & BYTE_MASK;
        for (ContentType contentType : ContentType.values()) {
            if (contentType.value == value) {
                return contentType;
            }
        }
        return null;
    }
}
