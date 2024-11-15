package samba.storage;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;


import java.util.Optional;

public interface HistoryDB {


    boolean saveContent(Bytes key, Bytes value);

    Optional<BlockHeader> getBlockHeader(Bytes blockHash);

    Bytes get(Bytes key);

    void delete(Bytes key);

    boolean contains(Bytes key);
}
