package samba.services;

import samba.domain.content.ContentBlockBody;
import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentReceipts;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryDB {

  boolean saveContent(Bytes sszContentKey, Bytes sszValue);

  Optional<ContentBlockHeader> getBlockHeaderByBlockHash(Bytes blockHash);

  Optional<Bytes> getBlockHashByBlockNumber(Bytes blockNumber);

  Optional<ContentBlockBody> getBlockBodyByBlockHash(Bytes blockHash);

  Optional<ContentReceipts> getReceiptsByBlockHash(Bytes blockHash);

  Optional<Bytes> get(ContentKey contentKey);

  boolean isAvailable();
}
