package samba.validation.util;

import static org.junit.jupiter.api.Assertions.*;

import samba.util.DefaultContent;

import java.util.Arrays;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.Hash;
import org.junit.jupiter.api.Test;

class ValidationUtilTest {

  @Test
  void testIsValidMerkleBranch() {
    Bytes32 leafB00 = Bytes32.fromHexString("0x" + "aa".repeat(32));
    Bytes32 leafB01 = Bytes32.fromHexString("0x" + "bb".repeat(32));
    Bytes32 leafB10 = Bytes32.fromHexString("0x" + "cc".repeat(32));
    Bytes32 leafB11 = Bytes32.fromHexString("0x" + "dd".repeat(32));

    Bytes32 nodeB0x = hashConcat(leafB00, leafB01);
    Bytes32 nodeB1x = hashConcat(leafB10, leafB11);
    Bytes32 root = hashConcat(nodeB0x, nodeB1x);

    assertTrue(
        ValidationUtil.isValidMerkleBranch(
            leafB00, Arrays.asList(leafB01, nodeB1x), 2, 0b00, root));

    assertTrue(
        ValidationUtil.isValidMerkleBranch(
            leafB01, Arrays.asList(leafB00, nodeB1x), 2, 0b01, root));

    assertTrue(
        ValidationUtil.isValidMerkleBranch(
            leafB10, Arrays.asList(leafB11, nodeB0x), 2, 0b10, root));

    assertTrue(
        ValidationUtil.isValidMerkleBranch(
            leafB11, Arrays.asList(leafB10, nodeB0x), 2, 0b11, root));

    assertTrue(ValidationUtil.isValidMerkleBranch(leafB11, List.of(leafB10), 1, 0b11, nodeB1x));

    // Ensure that incorrect proofs fail
    // Zero-length proof
    assertFalse(ValidationUtil.isValidMerkleBranch(leafB01, List.of(), 2, 0b01, root));

    // Proof in reverse order
    assertFalse(
        ValidationUtil.isValidMerkleBranch(
            leafB01, Arrays.asList(nodeB1x, leafB00), 2, 0b01, root));

    // Proof too short
    assertFalse(ValidationUtil.isValidMerkleBranch(leafB01, List.of(leafB00), 2, 0b01, root));

    // Wrong index
    assertFalse(
        ValidationUtil.isValidMerkleBranch(
            leafB01, Arrays.asList(leafB00, nodeB1x), 2, 0b10, root));

    // Wrong root
    assertFalse(
        ValidationUtil.isValidMerkleBranch(
            leafB01, Arrays.asList(leafB00, nodeB1x), 2, 0b01, nodeB1x));
  }

  @Test
  public void testIsBlockHeaderValid() {
    boolean resultPreMerge = ValidationUtil.isBlockHeaderValid(DefaultContent.preMergeBlockHeader);
    boolean resultPostCapella =
        ValidationUtil.isBlockHeaderValid(DefaultContent.postCapellaBlockHeader);
    assertTrue(resultPreMerge);
    assertTrue(resultPostCapella);
  }

  @Test
  public void testIsBlockBodyValid() {
    boolean resultPreMerge =
        ValidationUtil.isBlockBodyValid(
            DefaultContent.preMergeBlockHeader, DefaultContent.preMergeBlockBody.getSszBytes());
    boolean resultPostCapella =
        ValidationUtil.isBlockBodyValid(
            DefaultContent.postCapellaBlockHeader,
            DefaultContent.postCapellaBlockBody.getSszBytes());
    assertTrue(resultPreMerge);
    assertTrue(resultPostCapella);
  }

  @Test
  public void testIsReceiptsValid() {
    boolean resultPreMerge =
        ValidationUtil.isReceiptsValid(
            DefaultContent.preMergeBlockHeader, DefaultContent.preMergeReceipts.getSszBytes());
    boolean resultPostCapella =
        ValidationUtil.isReceiptsValid(
            DefaultContent.postCapellaBlockHeader,
            DefaultContent.postCapellaReceipts.getSszBytes());
    assertTrue(resultPreMerge);
    assertTrue(resultPostCapella);
  }

  private static Bytes32 hashConcat(Bytes32 left, Bytes32 right) {
    return Hash.sha256(Bytes.concatenate(left, right));
  }
}
