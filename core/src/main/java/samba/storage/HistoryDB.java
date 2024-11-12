package samba.storage;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryDB {


    void saveContent(Bytes key, Bytes value);

    Bytes get(Bytes key);

    void delete(Bytes key);

    boolean contains(Bytes key);
}
