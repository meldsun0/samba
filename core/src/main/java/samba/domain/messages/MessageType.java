package samba.domain.messages;

import static com.google.common.base.Preconditions.checkArgument;

/***
 * Unsupported messages SHOULD receive a TALKRESP message with an empty payload.
 */
public enum MessageType {

    PING(0x00),
    PONG(0x01),
    FIND_NODES(0x02),
    NODES(0x03),
    FIND_CONTENT(0x04),
    CONTENT(0x05),
    OFFER(0x06),
    ACCEPT(0x07);

    private final byte value;
    private static final int MAX_VALUE = 0x7F;
    private static final int BYTE_MASK = 0xFF;

    MessageType(int value) {
        checkArgument(value <= MAX_VALUE, "Packet type ID must be in range [0x00, 0x80)");
        this.value = (byte) (value & BYTE_MASK);

    }

    public byte getByteValue() {
        return value;
    }

    public static MessageType fromInt(int value) {
        value = value & BYTE_MASK;
        for (MessageType messageType : MessageType.values()) {
            if (messageType.value == value) {
                return messageType;
            }
        }
        return null;
    }
}



