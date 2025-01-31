package samba.domain.content;

import samba.schema.content.ssz.blockheader.BlockHeaderProofUnion;
import samba.schema.content.ssz.blockheader.BlockHeaderWithProofContainer;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.ethereum.core.BlockHeader;

public class ContentBlockHeader {

  private final BlockHeaderWithProofContainer blockHeaderWithProofContainer;
  private final ContentProofType proofType;

  public ContentBlockHeader(BlockHeaderWithProofContainer blockHeaderWithProofContainer) {
    this.blockHeaderWithProofContainer = blockHeaderWithProofContainer;
    this.proofType = blockHeaderWithProofContainer.getBlockHeaderProof().getProofType();
  }

  public ContentBlockHeader(Bytes sszBytes) {
    this.blockHeaderWithProofContainer = BlockHeaderWithProofContainer.decodeBytes(sszBytes);
    this.proofType = blockHeaderWithProofContainer.getBlockHeaderProof().getProofType();
  }

  public ContentBlockHeader(BlockHeader blockHeader, BlockHeaderProofUnion blockHeaderProof) {
    this.blockHeaderWithProofContainer =
        new BlockHeaderWithProofContainer(blockHeader, blockHeaderProof);
    this.proofType = blockHeaderProof.getProofType();
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
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalHashesAccumulator();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR");
  }

  public List<Bytes32> getBeaconBlockProofHistoricalRoots() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalRootsContainer()
          .getBeaconBlockProofHistoricalRoots();
    }
    throw new UnsupportedOperationException("Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS");
  }

  public Bytes32 getBeaconBlockRoot() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalRootsContainer()
          .getBlockRoot();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalSummariesContainer()
          .getBlockRoot();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS or BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public List<Bytes32> getExecutionBlockProof() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalRootsContainer()
          .getExecutionBlockProof();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalSummariesContainer()
          .getExecutionBlockProof();
    }
    throw new UnsupportedOperationException(
        "Block proof type is not BLOCK_PROOF_HISTORICAL_ROOTS or BLOCK_PROOF_HISTORICAL_SUMMARIES");
  }

  public long getSlot() {
    if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_ROOTS) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalRootsContainer()
          .getSlot()
          .longValue();
    } else if (proofType == ContentProofType.BLOCK_PROOF_HISTORICAL_SUMMARIES) {
      return blockHeaderWithProofContainer
          .getBlockHeaderProof()
          .getBlockProofHistoricalSummariesContainer()
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
}
