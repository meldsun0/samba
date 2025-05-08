package samba.domain.content;

import samba.network.history.HistoryConstants;
import samba.schema.content.ssz.blockheader.BlockHeaderWithProofContainer;
import samba.schema.content.ssz.blockheader.BlockProofHistoricalRootsContainer;
import samba.schema.content.ssz.blockheader.BlockProofHistoricalSummariesCapellaContainer;
import samba.schema.content.ssz.blockheader.SszBlockProofHistoricalHashesAccumulatorVector;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.ethereum.core.BlockHeader;

public class ContentBlockHeader {

  private final BlockHeaderWithProofContainer blockHeaderWithProofContainer;
  private final ContentProofType proofType;

  public ContentBlockHeader(BlockHeaderWithProofContainer blockHeaderWithProofContainer) {
    this.blockHeaderWithProofContainer = blockHeaderWithProofContainer;
    this.proofType = getContentProofTypeFromHeader(blockHeaderWithProofContainer.getBlockHeader());
  }

  public ContentBlockHeader(Bytes sszBytes) {
    this.blockHeaderWithProofContainer = BlockHeaderWithProofContainer.decodeBytes(sszBytes);
    this.proofType = getContentProofTypeFromHeader(blockHeaderWithProofContainer.getBlockHeader());
  }

  public ContentBlockHeader(BlockHeader blockHeader, Bytes encodedBlockHeaderProof) {
    this.blockHeaderWithProofContainer =
        new BlockHeaderWithProofContainer(blockHeader, encodedBlockHeaderProof);
    this.proofType = getContentProofTypeFromHeader(blockHeader);
  }

  public BlockHeaderWithProofContainer getBlockHeaderWithProofContainer() {
    return blockHeaderWithProofContainer;
  }

  public BlockHeader getBlockHeader() {
    return blockHeaderWithProofContainer.getBlockHeader();
  }

  public ContentProofType getBlockHeaderProofType() {
    return proofType;
  }

  public List<Bytes32> getBlockProofHistoricalHashesAccumulator() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR) {
      return SszBlockProofHistoricalHashesAccumulatorVector.decodeVector(
          blockHeaderWithProofContainer.getEncodedBlockHeaderProof());
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR");
  }

  public List<Bytes32> getBeaconBlockProofHistoricalRoots() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return BlockProofHistoricalRootsContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getBeaconBlockProofHistoricalRoots();
    }
    throw new UnsupportedOperationException("Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS");
  }

  public List<Bytes32> getBeaconBlockProofHistoricalSummaries() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return BlockProofHistoricalSummariesCapellaContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getBeaconBlockProofHistoricalSummaries();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public Bytes32 getBeaconBlockRoot() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return BlockProofHistoricalRootsContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getBlockRoot();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return BlockProofHistoricalSummariesCapellaContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getBlockRoot();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS or BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public List<Bytes32> getExecutionBlockProof() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return BlockProofHistoricalRootsContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getExecutionBlockProof();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return BlockProofHistoricalSummariesCapellaContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getExecutionBlockProof();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS or BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public long getSlot() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return BlockProofHistoricalRootsContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getSlot()
          .longValue();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return BlockProofHistoricalSummariesCapellaContainer.decodeBytes(
              blockHeaderWithProofContainer.getEncodedBlockHeaderProof())
          .getSlot()
          .longValue();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS or BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public Bytes getSszBytes() {
    return blockHeaderWithProofContainer.sszSerialize();
  }

  public static ContentBlockHeader decode(Bytes sszBytes) {
    return new ContentBlockHeader(sszBytes);
  }

  public static ContentProofType getContentProofTypeFromHeader(BlockHeader blockHeader) {
    long timestamp = blockHeader.getTimestamp();
    if (timestamp < HistoryConstants.MERGE_TIMESTAMP)
      return ContentProofType.BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR;
    else if (timestamp < HistoryConstants.SHANGHAI_TIMESTAMP)
      return ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS;
    else return ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES;
  }
}
