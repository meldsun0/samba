package samba.validation.util;

import samba.domain.content.ContentBlockBody;
import samba.domain.content.ContentBlockHeader;
import samba.domain.content.ContentProofType;
import samba.domain.content.ContentReceipts;
import samba.network.history.HistoryConstants;
import samba.validation.HistoricalHashesAccumulator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.Hash;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.rlp.RLP;
import org.hyperledger.besu.ethereum.trie.NodeLoader;
import org.hyperledger.besu.ethereum.trie.patricia.StoredMerklePatriciaTrie;
import org.hyperledger.besu.ethereum.trie.patricia.StoredNodeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

  private static final HistoricalHashesAccumulator historicalHashesAccumulator =
      new HistoricalHashesAccumulator();

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
    if (blockHeaderWithProof.getBlockHeaderProofType()
        == ContentProofType.BLOCK_PROOF_HISTORICAL_HASHES_ACCUMULATOR) {
      return historicalHashesAccumulator.validate(blockHeaderWithProof);
    } else { // TODO: add other proof type validation
      return true;
    }
  }

  public static boolean isBlockBodyValid(ContentBlockHeader blockHeader, Bytes blockBodyBytes) {
    BlockHeader blockHeaderForBody = blockHeader.getBlockHeader();
    ContentBlockBody body = ContentBlockBody.decode(blockBodyBytes);
    if (!blockHeaderForBody
        .getTransactionsRoot()
        .equals(computeTransactionsRoot(body.getTransactionsRLP()))) {
      LOG.error("Invalid transactions root for block {}", blockHeaderForBody.getHash());
      return false;
    }
    if (!blockHeaderForBody.getOmmersHash().equals(Hash.keccak256(body.getUnclesRLP()))) {
      LOG.error("Invalid uncles root for block {}", blockHeaderForBody.getHash());
      return false;
    }
    if (body.getBlockNumber() >= HistoryConstants.SHANGHAI_BLOCK) {
      Optional<org.hyperledger.besu.datatypes.Hash> headerWithdrawalsRoot =
          blockHeaderForBody.getWithdrawalsRoot();
      if (!headerWithdrawalsRoot.get().equals(body.getWithdrawalsSsz().hashTreeRoot())) {
        LOG.error("Invalid withdrawals root for block {}", blockHeaderForBody.getHash());
        return false;
      }
    }
    return true;
  }

  public static boolean isReceiptsValid(ContentBlockHeader blockHeader, Bytes receipts) {
    BlockHeader blockHeaderFromBody = blockHeader.getBlockHeader();
    ContentReceipts contentReceipts = ContentReceipts.decode(receipts);
    if (!blockHeaderFromBody
        .getReceiptsRoot()
        .equals(contentReceipts.getSszReceiptList().getEncodedList().hashTreeRoot())) {
      return false;
    }
    return true;
  }

  private static Bytes32 computeTransactionsRoot(final List<Bytes> transactions) {
    Map<Bytes32, Bytes> storage = new HashMap<>();
    NodeLoader nodeLoader = (location, hash) -> Optional.ofNullable(storage.get(hash));
    Function<Bytes, Bytes> identity = Function.identity();
    StoredNodeFactory<Bytes> nodeFactory = new StoredNodeFactory<>(nodeLoader, identity, identity);
    StoredMerklePatriciaTrie<Bytes, Bytes> trie =
        new StoredMerklePatriciaTrie<>(nodeFactory, Bytes32.ZERO);

    for (int i = 0; i < transactions.size(); i++) {
      final int index = i;
      Bytes transactionIndex = RLP.encode(out -> out.writeIntScalar(index));
      Bytes encodedTransaction = transactions.get(index);
      trie.put(transactionIndex, encodedTransaction);
    }
    return trie.getRootHash();
  }
}
