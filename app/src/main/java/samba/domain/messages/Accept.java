package samba.domain.messages;

import org.apache.tuweni.units.bigints.UInt64;

import java.util.BitSet;

/***
 * Response message to Offer (0x06).
 */
public class Accept {

    private final UInt64 connectionId;
    private final BitSet content_keys;

    public Accept(UInt64 connectionId, BitSet contentKeys) {
        this.connectionId = connectionId;
        content_keys = contentKeys;
    }
}
