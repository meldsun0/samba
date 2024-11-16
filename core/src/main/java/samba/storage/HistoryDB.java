package samba.storage;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockWithReceipts;
import samba.domain.content.ContentType;


import java.util.Optional;

public interface HistoryDB {

    boolean saveContent(Bytes key, Bytes value);

    Optional<BlockHeader> getBlockHeaderByBlockHash(Bytes blockHash);

    Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumber);

    Optional<BlockBody> getBlockBodyByBlockHash(Bytes blockHash);

    Optional<BlockWithReceipts> getBlockReceiptByBlockHash(Bytes blockHash);     //TODO or return List<TransactionReceipt>

    Optional<byte[]> get(ContentType contentType, Bytes contentKey);
}
