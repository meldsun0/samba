package samba.domain.messages;

/**
 * Request message to offer a set of content_keys that this node has content available for.
 */
public class Offer {

    private final Byte[][] content_keys;

    public Offer(Byte[][] contentKeys) {
        content_keys = contentKeys;
    }
}
