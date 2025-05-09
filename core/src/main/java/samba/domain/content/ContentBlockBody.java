package samba.domain.content;

import samba.network.history.HistoryConstants;
import samba.schema.content.ssz.blockbody.BlockBodyPostShanghaiContainer;
import samba.schema.content.ssz.blockbody.BlockBodyPreShanghaiContainer;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.Withdrawal;

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

  public long getNumber() {
    return blockNumber;
  }

  public List<Transaction> getTransactions() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getTransactions();
    } else {
      return blockBodyPostShanghaiContainer.getTransactions();
    }
  }

  public List<BlockHeader> getUncles() {
    if (blockNumber < HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPreShanghaiContainer.getUncles();
    } else {
      return blockBodyPostShanghaiContainer.getUncles();
    }
  }

  public List<Withdrawal> getWithdrawals() {
    if (blockNumber >= HistoryConstants.SHANGHAI_BLOCK) {
      return blockBodyPostShanghaiContainer.getWithdrawals();
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
}
