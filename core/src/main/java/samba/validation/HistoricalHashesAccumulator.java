package samba.validation;

import samba.domain.content.ContentBlockHeader;
import samba.network.history.HistoryConstants;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalHashesAccumulatorContainer;
import samba.validation.util.ValidationUtil;

import java.io.InputStream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;

public class HistoricalHashesAccumulator {
  public static final int EPOCH_SIZE = 8192;
  public static final int MAX_HISTORICAL_EPOCHS = 2048;
  public static final long HISTORICAL_EPOCHS_GINDEX = 3L;
  public static final int TREE_DEPTH = 15;

  private HistoricalHashesAccumulatorContainer historicalHashesAccumulatorContainer;

  public HistoricalHashesAccumulator() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("premergeacc.bin");
      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      historicalHashesAccumulatorContainer =
          HistoricalHashesAccumulatorContainer.decodeBytes(accumulatorBytes);
    } catch (Exception e) {
      this.historicalHashesAccumulatorContainer = null;
      throw new RuntimeException(e);
    }
  }

  public boolean validate(final ContentBlockHeader blockHeaderWithProof) {
    if (historicalHashesAccumulatorContainer == null) {
      throw new IllegalStateException("Historical hashes accumulator is not initialized.");
    }
    return validate(blockHeaderWithProof, historicalHashesAccumulatorContainer);
  }

  public static boolean validate(
      final ContentBlockHeader blockHeaderWithProof,
      final HistoricalHashesAccumulatorContainer accumulator) {
    if (blockHeaderWithProof.getBlockHeader().getNumber() >= HistoryConstants.MERGE_BLOCK) {
      return false;
    }
    int headerIndex = (int) blockHeaderWithProof.getBlockHeader().getNumber() % EPOCH_SIZE;
    long generalIndex = (EPOCH_SIZE * 2 * 2) + (headerIndex * 2);
    int epochIndex = (int) blockHeaderWithProof.getBlockHeader().getNumber() / EPOCH_SIZE;
    Bytes32 epochHash = accumulator.getHistoricalEpochs().get(epochIndex);

    return ValidationUtil.isValidMerkleBranch(
        blockHeaderWithProof.getBlockHeader().getBlockHash(),
        blockHeaderWithProof.getBlockProofHistoricalHashesAccumulator(),
        TREE_DEPTH,
        (int) generalIndex,
        epochHash);
  }

  // TODO: add option to build accumulator from scratch
}
