package samba.services.storage;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryDB {

    public void put(Bytes key, Bytes value);

    public Bytes get(Bytes key);

    public void delete(Bytes key);

    public boolean contains(Bytes key);
}
