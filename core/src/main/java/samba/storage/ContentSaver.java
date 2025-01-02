package samba.storage;

import org.apache.tuweni.bytes.Bytes;

@FunctionalInterface
public interface ContentSaver {

    boolean saveContent(Bytes contentKey, Bytes value);
}
