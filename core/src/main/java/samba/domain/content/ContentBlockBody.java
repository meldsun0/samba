package samba.domain.content;

import samba.network.history.HistoryConstants;
import samba.schema.content.ssz.blockbody.BlockBodyPostShanghaiContainer;
import samba.schema.content.ssz.blockbody.BlockBodyPreShanghaiContainer;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;

public class ContentBlockBody {

  private final BlockBodyPreShanghaiContainer blockBodyPreShanghaiContainer;
  private final BlockBodyPostShanghaiContainer blockBodyPostShanghaiContainer;
  private final long blockNumber;

  public ContentBlockBody(Bytes sszBytes) {
    BlockBodyPreShanghaiContainer tempBlockBodyPreShanghaiContainer;
    BlockBodyPostShanghaiContainer tempBlockBodyPostShanghaiContainer;
    long tempBlockNumber;
    try {
      tempBlockBodyPreShanghaiContainer = BlockBodyPreShanghaiContainer.decodeBytes(sszBytes);
      tempBlockBodyPostShanghaiContainer = null;
      tempBlockNumber =
          HistoryConstants.SHANGHAI_BLOCK - 1; // No guaranteed way to get timestamp from body
    } catch (Exception e) {
      tempBlockBodyPreShanghaiContainer = null;
      tempBlockBodyPostShanghaiContainer = BlockBodyPostShanghaiContainer.decodeBytes(sszBytes);
      tempBlockNumber = HistoryConstants.SHANGHAI_BLOCK;
    }
    this.blockBodyPreShanghaiContainer = tempBlockBodyPreShanghaiContainer;
    this.blockBodyPostShanghaiContainer = tempBlockBodyPostShanghaiContainer;
    this.blockNumber = tempBlockNumber;
  }

  public ContentBlockBody(
      BlockBodyPreShanghaiContainer blockBodyPreShanghaiContainer, long blockNumber) {
    this.blockBodyPreShanghaiContainer = blockBodyPreShanghaiContainer;
    this.blockBodyPostShanghaiContainer = null;
    this.blockNumber = blockNumber;
  }

  public ContentBlockBody(
      BlockBodyPostShanghaiContainer blockBodyPostShanghaiContainer, long blockNumber) {
    this.blockBodyPreShanghaiContainer = null;
    this.blockBodyPostShanghaiContainer = blockBodyPostShanghaiContainer;
    this.blockNumber = blockNumber;
  }

  public BlockBodyPreShanghaiContainer getBlockBodyPreShanghaiContainer() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer;
    }
    throw new UnsupportedOperationException("Block body is post-Shanghai");
  }

  public BlockBodyPostShanghaiContainer getBlockBodyPostShanghaiContainer() {
    if (blockNumber >= HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPostShanghaiContainer;
    }
    throw new UnsupportedOperationException("Block body is pre-Shanghai");
  }

  public long getBlockNumber() {
    return blockNumber;
  }

  public List<Transaction> getTransactions() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getTransactions();
    } else {
      return blockBodyPostShanghaiContainer.getTransactions();
    }
  }

  public List<Bytes> getTransactionsRLP() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getTransactionsRLP();
    } else {
      return blockBodyPostShanghaiContainer.getTransactionsRLP();
    }
  }

  public List<BlockHeader> getUncles() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getUncles();
    } else {
      return blockBodyPostShanghaiContainer.getUncles();
    }
  }

  public Bytes getUnclesRLP() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getUnclesRLP();
    } else {
      return blockBodyPostShanghaiContainer.getUnclesRLP();
    }
  }

  public Optional<List<Withdrawal>> getWithdrawals() {
    if (blockNumber >= HistoryConstants.SHANGHAI_BLOCK) {
      return Optional.ofNullable(blockBodyPostShanghaiContainer.getWithdrawals());
    }
    return Optional.empty();
  }

  public SszList<SszByteList> getWithdrawalsSsz() {
    if (blockNumber >= HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPostShanghaiContainer.getWithdrawalsSsz();
    }
    throw new UnsupportedOperationException("Block body is pre-Shanghai");
  }

  public Bytes getSszBytes() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.sszSerialize();
    } else {
      return blockBodyPostShanghaiContainer.sszSerialize();
    }
  }

  public static ContentBlockBody decode(Bytes sszBytes) {
    return new ContentBlockBody(sszBytes);
  }

  public BlockBody getBlockBody() {
    return new BlockBody(this.getTransactions(), this.getUncles(), this.getWithdrawals());
  }
}
