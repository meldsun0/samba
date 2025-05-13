package samba.network.history.api.methods;

import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class GetBlockHeaderByBlockNumber {

  private static final Logger LOG = LoggerFactory.getLogger(GetBlockHeaderByBlockNumber.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetBlockHeaderByBlockNumber(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<BlockHeader> execute(final UInt64 blockNumber) {
    ContentKey contentKey = new ContentKey(ContentType.BLOCK_HEADER_BY_NUMBER, blockNumber);
    return GetContent.execute(
        historyNetworkInternalAPI,
        contentKey.getSszBytes(),
        (bytes) -> Optional.ofNullable(ContentBlockHeader.decode(bytes).getBlockHeader()));
  }

  public static Optional<BlockHeader> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final UInt64 blockNumber) {
    LOG.debug("Executing GetBlockHeaderByBlockNumber with parameters blockNumber:{}", blockNumber);
    return new GetBlockHeaderByBlockNumber(historyNetworkInternalAPI).execute(blockNumber);
  }
}
