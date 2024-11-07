package samba.domain.content;

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

    public static ContentType fromInt(int value) {
        value = value & BYTE_MASK;
        for (ContentType contentType : ContentType.values()) {
            if (contentType.value == value) {
                return contentType;
            }
        }
        return null;
    }
}
