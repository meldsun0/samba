package samba.validation.util;

import samba.domain.content.ContentBlockBody;
import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentReceipts;
import samba.network.history.HistoryConstants;
import samba.validation.HistoricalHashesAccumulator;
import samba.validation.HistoricalRootsAccumulator;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.trie.patricia.SimpleMerklePatriciaTrie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

  private static final HistoricalHashesAccumulator historicalHashesAccumulator =
      new HistoricalHashesAccumulator();
  private static final HistoricalRootsAccumulator historicalRootsAccumulator =
      new HistoricalRootsAccumulator();

  public static boolean isValidMerkleBranch(
      final Bytes32 leaf,
      final List<Bytes32> branch,
      final int depth,
      final int startingIndex,
      final Bytes32 root) {

    try {
      Bytes32 value = leaf;
      int index = startingIndex;

      for (int i = 0; i < depth; i++) {
        Bytes32 sibling = branch.get(i);
        Bytes combined;
        if ((index & 1) == 1) {
          // index bit is 1: sibling on the left
          combined = Bytes.concatenate(sibling, value);
        } else {
          // index bit is 0: sibling on the right
          combined = Bytes.concatenate(value, sibling);
        }
        value = Hash.sha256(combined);
        index >>>= 1;
      }
      return value.equals(root);
    } catch (IndexOutOfBoundsException e) {
      LOG.error("Invalid Proof: {}", e.getMessage());
      return false;
    }
  }

  public static boolean isBlockHeaderValid(ContentBlockHeader blockHeaderWithProof) {
    try {
      switch (blockHeaderWithProof.getBlockHeaderProofType()) {
        case BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR:
          return historicalHashesAccumulator.validate(blockHeaderWithProof);
        case BLOCK_PROOF_HISTORICAL_ROOTS:
          return historicalRootsAccumulator.validate(blockHeaderWithProof);
        default:
          // TODO: add other proof type validation
          return true;
      }
    } catch (Exception e) {
      LOG.debug("Error validating block header: {}", e.getMessage());
      return false;
    }
  }

  public static boolean isBlockBodyValid(ContentBlockHeader blockHeader, Bytes blockBodyBytes) {
    try {
      BlockHeader blockHeaderForBody = blockHeader.getBlockHeader();
      ContentBlockBody body = ContentBlockBody.decode(blockBodyBytes);
      if (!blockHeaderForBody
          .getTransactionsRoot()
          .equals(computeRoot(body.getTransactionsRLP()))) {
        LOG.debug("Invalid transactions root for block {}", blockHeaderForBody.getHash());
        return false;
      }
      if (!blockHeaderForBody.getOmmersHash().equals(Hash.keccak256(body.getUnclesRLP()))) {
        LOG.debug("Invalid uncles root for block {}", blockHeaderForBody.getHash());
        return false;
      }
      if (body.getBlockNumber() >= HistoryConstants.SHANGHAI_BLOCK) {
        Optional<org.hyperledger.besu.datatypes.Hash> headerWithdrawalsRoot =
            blockHeaderForBody.getWithdrawalsRoot();
        if (!headerWithdrawalsRoot.get().equals(computeRoot(body.getWithdrawalsRLP().get()))) {
          LOG.debug("Invalid withdrawals root for block {}", blockHeaderForBody.getHash());
          return false;
        }
      }
      return true;
    } catch (Exception e) {
      LOG.debug("Error validating block body: {}", e.getMessage());
      return false;
    }
  }

  public static boolean isReceiptsValid(ContentBlockHeader blockHeader, Bytes receipts) {
    try {
      BlockHeader blockHeaderFromBody = blockHeader.getBlockHeader();
      ContentReceipts contentReceipts = ContentReceipts.decode(receipts);
      if (!blockHeaderFromBody
          .getReceiptsRoot()
          .equals(computeRoot(contentReceipts.getReceiptsRLP()))) {
        LOG.debug("Invalid receipts root for block {}", blockHeaderFromBody.getHash());
        return false;
      }
      return true;
    } catch (Exception e) {
      LOG.debug("Error validating receipts: {}", e.getMessage());
      return false;
    }
  }

  public static Bytes32 computeRoot(final List<Bytes> nodes) {
    Function<Bytes, Bytes> identity = Function.identity();

    SimpleMerklePatriciaTrie<Bytes, Bytes> trie = new SimpleMerklePatriciaTrie<>(identity);

    for (int i = 0; i < nodes.size(); i++) {
      final int index = i;
      Bytes transactionIndex = RLP.encode(out -> out.writeIntScalar(index)); // RLP index
      Bytes encodedTransaction = nodes.get(index); // Already RLP encoded
      trie.put(transactionIndex, encodedTransaction);
    }

    return trie.getRootHash();
  }
}
