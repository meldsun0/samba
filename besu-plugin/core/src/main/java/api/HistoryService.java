package api;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.plugin.Unstable;
import org.hyperledger.besu.plugin.services.BesuService;

@Unstable
public interface HistoryService extends BesuService {

  BlockHeader getBlockHeaderByBlockHash(Hash blockHash);

  BlockBody getBlockBodyByBlockHash(Hash blockHash);

  TransactionReceipt getReceiptByBlockHash(Hash blockHash);

  BlockHeader getBlockHeaderByBlockNumber(long blockNumber);
}
