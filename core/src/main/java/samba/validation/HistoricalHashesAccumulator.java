package samba.validation;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt256;
import org.hyperledger.besu.ethereum.core.BlockHeader;

import samba.schema.content.ssz.blockheader.accumulator.EpochRecordList;
import samba.schema.content.ssz.blockheader.accumulator.HeaderRecordContainer;
import samba.schema.content.ssz.blockheader.accumulator.HistoricalHashesAccumulatorContainer;

public class HistoricalHashesAccumulator {
  public static final int EPOCH_SIZE = 8192;
  public static final int MAX_HISTORICAL_EPOCHS = 2048;

  public static void updateAccumulator(HistoricalHashesAccumulatorContainer accumulator, BlockHeader newBlockHeader) {
    UInt256 lastTotalDifficulty;
    List<HeaderRecordContainer> currentEpoch = new ArrayList<>(accumulator.getEpochRecord());
    List<Bytes32> historicalEpochs = new ArrayList<>(accumulator.getHistoricalEpochs());
    if (accumulator.getEpochRecord().isEmpty()) {
      lastTotalDifficulty = UInt256.ZERO;
    } else {
      lastTotalDifficulty = accumulator.getEpochRecord().get(accumulator.getEpochRecord().size() - 1).getTotalDifficulty();
    }

    if (accumulator.getEpochRecord().size() == HistoricalHashesAccumulator.EPOCH_SIZE) {
      EpochRecordList fullEpoch = new EpochRecordList(new ArrayList<>(accumulator.getEpochRecord()));
      Bytes32 epochHash = fullEpoch.getEncodedList().hashTreeRoot();
      historicalEpochs.add(epochHash);
      currentEpoch = new ArrayList<>();
    }

    UInt256 newTotalDifficulty = lastTotalDifficulty.add(UInt256.valueOf(newBlockHeader.getDifficulty().getAsBigInteger()));
    HeaderRecordContainer newRecord =
        new HeaderRecordContainer(newBlockHeader.getHash(), newTotalDifficulty);
    currentEpoch.add(newRecord);

    accumulator.getEpochRecord().add(newRecord);
  }
}
