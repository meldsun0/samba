package samba.storage;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentType;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockWithReceipts;

public interface HistoryDB {

  boolean saveContent(ContentKey contentKey, Bytes value);

  Optional<BlockHeader> getBlockHeaderByBlockHash(Bytes blockHash);

  Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumber);

  Optional<BlockBody> getBlockBodyByBlockHash(Bytes blockHash);

  Optional<BlockWithReceipts> getBlockReceiptByBlockHash(
      Bytes blockHash); // TODO or return List<TransactionReceipt>

  Optional<byte[]> get(ContentType contentType, Bytes contentKey);
}
