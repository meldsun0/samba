package samba.storage;

import samba.domain.content.*;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryDB {

  boolean saveContent(Bytes sszContentKey, Bytes sszValue);

  Optional<ContentBlockHeader> getBlockHeaderByBlockHash(Bytes blockHash);

  Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumber);

  Optional<ContentBlockBody> getBlockBodyByBlockHash(Bytes blockHash);

  Optional<ContentReceipts> getReceiptsByBlockHash(Bytes blockHash);

  Optional<Bytes> get(ContentKey contentKey);
}
