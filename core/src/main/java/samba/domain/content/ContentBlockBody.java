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
  private final long timestamp;

  public ContentBlockBody(Bytes sszBytes) {
    BlockBodyPreShanghaiContainer tempBlockBodyPreShanghaiContainer;
    BlockBodyPostShanghaiContainer tempBlockBodyPostShanghaiContainer;
    long tempTimestamp;
    try {
      tempBlockBodyPreShanghaiContainer = BlockBodyPreShanghaiContainer.decodeBytes(sszBytes);
      tempBlockBodyPostShanghaiContainer = null;
      tempTimestamp =
          HistoryConstants.SHANGHAI_TIMESTAMP - 1; // No guaranteed way to get timestamp from body
    } catch (Exception e) {
      tempBlockBodyPreShanghaiContainer = null;
      tempBlockBodyPostShanghaiContainer = BlockBodyPostShanghaiContainer.decodeBytes(sszBytes);
      tempTimestamp = HistoryConstants.SHANGHAI_TIMESTAMP;
    }
    this.blockBodyPreShanghaiContainer = tempBlockBodyPreShanghaiContainer;
    this.blockBodyPostShanghaiContainer = tempBlockBodyPostShanghaiContainer;
    this.timestamp = tempTimestamp;
  }

  public ContentBlockBody(
      BlockBodyPreShanghaiContainer blockBodyPreShanghaiContainer, long timestamp) {
    this.blockBodyPreShanghaiContainer = blockBodyPreShanghaiContainer;
    this.blockBodyPostShanghaiContainer = null;
    this.timestamp = timestamp;
  }

  public ContentBlockBody(
      BlockBodyPostShanghaiContainer blockBodyPostShanghaiContainer, long timestamp) {
    this.blockBodyPreShanghaiContainer = null;
    this.blockBodyPostShanghaiContainer = blockBodyPostShanghaiContainer;
    this.timestamp = timestamp;
  }

  public BlockBodyPreShanghaiContainer getBlockBodyPreShanghaiContainer() {
    if (timestamp < HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPreShanghaiContainer;
    }
    throw new UnsupportedOperationException("Block body is post-Shanghai");
  }

  public BlockBodyPostShanghaiContainer getBlockBodyPostShanghaiContainer() {
    if (timestamp >= HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPostShanghaiContainer;
    }
    throw new UnsupportedOperationException("Block body is pre-Shanghai");
  }

  public long getTimestamp() {
    return timestamp;
  }

  public List<Transaction> getTransactions() {
    if (timestamp < HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPreShanghaiContainer.getTransactions();
    } else {
      return blockBodyPostShanghaiContainer.getTransactions();
    }
  }

  public List<BlockHeader> getUncles() {
    if (timestamp < HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPreShanghaiContainer.getUncles();
    } else {
      return blockBodyPostShanghaiContainer.getUncles();
    }
  }

  public List<Withdrawal> getWithdrawals() {
    if (timestamp >= HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPostShanghaiContainer.getWithdrawals();
    }
    throw new UnsupportedOperationException("Block body is pre-Shanghai");
  }

  public Bytes getSszBytes() {
    if (timestamp < HistoryConstants.SHANGHAI_TIMESTAMP) {
      return blockBodyPreShanghaiContainer.sszSerialize();
    } else {
      return blockBodyPostShanghaiContainer.sszSerialize();
    }
  }
}
