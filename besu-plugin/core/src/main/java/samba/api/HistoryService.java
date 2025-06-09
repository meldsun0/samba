package samba.api;

import java.util.List;
import java.util.Optional;

import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.plugin.Unstable;

@Unstable
public interface HistoryService {

  Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash);

  Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash);

  Optional<List<TransactionReceipt>> getTransactionReceiptByBlockHash(Hash blockHash);

  //The characters in the string must all be decimal digits
  Optional<BlockHeader> getBlockHeaderByBlockNumber(String blockNumber);
}
