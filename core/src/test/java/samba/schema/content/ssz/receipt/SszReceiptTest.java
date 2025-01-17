package samba.schema.content.ssz.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszReceiptTest {

  TransactionReceipt transactionReceipt;

  @BeforeEach
  public void setUp() {
    this.transactionReceipt =
        new TransactionReceipt(
            Hash.hash(Bytes.fromHexString("0x1234")), 1, List.of(), Optional.empty());
  }

  @Test
  public void testSszDecode() {
    SszReceipt sszReceipt =
        new SszReceipt(
            Bytes.fromHexString(
                "0xf90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743201b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0"));
    TransactionReceipt decodedTransactionReceipt = sszReceipt.getDecodedReceipt();
    assertTrue(this.transactionReceipt.equals(decodedTransactionReceipt));
  }

  @Test
  public void testSszEncode() {
    SszReceipt sszReceipt = new SszReceipt(this.transactionReceipt);
    Bytes encodedReceipt = sszReceipt.sszSerialize();
    assertEquals(
        encodedReceipt,
        Bytes.fromHexString(
            "0xf90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743201b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0"));
  }
}
