package samba.validation;

import samba.domain.content.ContentBlockHeader;
import samba.network.history.HistoryConstants;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalRootsAccumulatorList;
import samba.validation.util.ValidationUtil;

import java.io.InputStream;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoricalRootsAccumulator {

  private static final Logger LOG = LoggerFactory.getLogger(HistoricalRootsAccumulator.class);

  public static final long HISTORICAL_ROOTS_LIMIT = 1L << 24;
  public static final int TREE_DEPTH = 14;

  private HistoricalRootsAccumulatorList historicalRootsList;

  public HistoricalRootsAccumulator() {
    try {
      InputStream file = getClass().getClassLoader().getResourceAsStream("historicalroots.bin");
      Bytes accumulatorBytes = Bytes.wrap(file.readAllBytes());
      historicalRootsList = HistoricalRootsAccumulatorList.decodeBytes(accumulatorBytes);
    } catch (Exception e) {
      this.historicalRootsList = null;
      throw new RuntimeException(e);
    }
  }

  public boolean validate(final ContentBlockHeader blockHeaderWithProof) {
    if (historicalRootsList == null) {
      LOG.error("Historical roots accumulator is not initialized.");
      throw new IllegalStateException("Historical roots accumulator is not initialized.");
    }
    return validate(blockHeaderWithProof, historicalRootsList);
  }

  public static boolean validate(
      final ContentBlockHeader blockHeaderWithProof,
      final HistoricalRootsAccumulatorList historicalRootsList) {
    if (blockHeaderWithProof.getBlockHeader().getNumber() < HistoryConstants.MERGE_BLOCK
        || blockHeaderWithProof.getBlockHeader().getNumber() >= HistoryConstants.SHANGHAI_BLOCK) {
      return false;
    }

    if (!ValidationUtil.isValidMerkleBranch(
        blockHeaderWithProof.getBlockHeader().getHash(),
        blockHeaderWithProof.getExecutionBlockProof(),
        blockHeaderWithProof.getExecutionBlockProof().size(),
        3228,
        blockHeaderWithProof.getBeaconBlockRoot())) {
      return false;
    }

    long slot = blockHeaderWithProof.getSlot();
    long blockRootIndex = slot % HistoryConstants.EPOCH_SIZE;
    long generalIndex = 2 * HistoryConstants.EPOCH_SIZE + blockRootIndex;
    int historicalRootIndex = (int) slot / HistoryConstants.EPOCH_SIZE;
    Bytes32 historicalRoot = historicalRootsList.getDecodedList().get(historicalRootIndex);

    return ValidationUtil.isValidMerkleBranch(
        blockHeaderWithProof.getBeaconBlockRoot(),
        blockHeaderWithProof.getBeaconBlockProofHistoricalRoots(),
        TREE_DEPTH,
        (int) generalIndex,
        historicalRoot);
  }

  // TODO: add option to build accumulator from scratch
}
