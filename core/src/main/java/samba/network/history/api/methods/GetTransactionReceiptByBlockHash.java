package samba.network.history.api.methods;

import samba.domain.content.ContentKey;
import samba.domain.content.ContentReceipts;
import samba.domain.content.ContentType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.List;
import java.util.Optional;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetTransactionReceiptByBlockHash {

  private static final Logger LOG = LoggerFactory.getLogger(GetTransactionReceiptByBlockHash.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetTransactionReceiptByBlockHash(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<List<TransactionReceipt>> execute(final Hash blockHash) {
    ContentKey contentKey = new ContentKey(ContentType.RECEIPT, blockHash);
    return GetContent.execute(
        historyNetworkInternalAPI,
        contentKey.getSszBytes(),
        (bytes) -> Optional.ofNullable(ContentReceipts.decode(bytes).getTransactionReceipts()));
  }

  public static Optional<List<TransactionReceipt>> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Hash blockHash) {
    LOG.debug("Executing GetTransactionReceiptByBlockHash with parameters blockHash:{}", blockHash);
    return new GetTransactionReceiptByBlockHash(historyNetworkInternalAPI).execute(blockHash);
  }
}
