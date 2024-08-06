package samba.domain.messages;

import static com.google.common.base.Preconditions.checkArgument;

public enum ContentSelectors {
    CONNECTION_ID(0x00),
    CONTENT(0x01),
    ENRS(0x02);

    private final byte value;

    private static final int BYTE_MASK = 0xFF;

    ContentSelectors(int value) {
        this.value = (byte) (value & BYTE_MASK);
    }

}



