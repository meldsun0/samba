package samba.validation.util;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.Hash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil {
  private static final Logger LOG = LoggerFactory.getLogger(ValidationUtil.class);

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
}
