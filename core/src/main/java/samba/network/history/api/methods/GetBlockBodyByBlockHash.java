package samba.network.history.api.methods;

import samba.domain.content.ContentBlockBody;
import samba.domain.content.ContentKey;
import samba.domain.content.ContentType;
import samba.network.history.api.HistoryNetworkInternalAPI;

import java.util.Optional;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetBlockBodyByBlockHash {

  private static final Logger LOG = LoggerFactory.getLogger(GetBlockBodyByBlockHash.class);

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public GetBlockBodyByBlockHash(HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  private Optional<BlockBody> execute(final Hash blockHash) {
    ContentKey contentKey = new ContentKey(ContentType.BLOCK_BODY, blockHash);
    return GetContent.execute(
        historyNetworkInternalAPI,
        contentKey.getSszBytes(),
        (bytes) -> Optional.ofNullable(ContentBlockBody.decode(bytes).getBlockBody()));
  }

  public static Optional<BlockBody> execute(
      final HistoryNetworkInternalAPI historyNetworkInternalAPI, final Hash blockHash) {
    LOG.debug("Executing GetBlockBodyByBlockHash with parameters blockHash:{}", blockHash);
    return new GetBlockBodyByBlockHash(historyNetworkInternalAPI).execute(blockHash);
  }
}
