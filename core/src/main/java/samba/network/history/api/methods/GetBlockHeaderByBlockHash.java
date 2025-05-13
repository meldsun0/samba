package samba.network.history.api.methods;

import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBlockHeaderByBlockHash {

  private static final Logger LOG = LoggerFactory.getLogger(GetBlockHeaderByBlockHash.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetBlockHeaderByBlockHash(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<BlockHeader> execute(final Hash blockHash) {
    ContentKey contentKey = new ContentKey(ContentType.BLOCK_HEADER, blockHash);
    return GetContent.execute(
        historyNetworkInternalAPI,
        contentKey.getSszBytes(),
        (bytes) -> Optional.ofNullable(ContentBlockHeader.decode(bytes).getBlockHeader()));
  }

  public static Optional<BlockHeader> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Hash blockHash) {
    LOG.debug("Executing GetBlockHeaderByBlockHash with parameters blockHash:{}", blockHash);
    return new GetBlockHeaderByBlockHash(historyNetworkInternalAPI).execute(blockHash);
  }
}
