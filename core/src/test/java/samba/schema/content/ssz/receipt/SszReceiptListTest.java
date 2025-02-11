package samba.schema.content.ssz.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszReceiptListTest {

  List<TransactionReceipt> transactionReceipts;

  @BeforeEach
  public void setup() {
    this.transactionReceipts = new ArrayList<>();
    for (int i = 0; i < 3; i++) {
      this.transactionReceipts.add(
          new TransactionReceipt(
              Hash.hash(Bytes.fromHexString("0x1234")), 1 + i, List.of(), Optional.empty()));
    }
  }

  @Test
  public void testSszDecode() {
    SszReceiptList sszReceiptList =
        new SszReceiptList(
            Bytes.fromHexString(
                "0x0c000000350100005e020000f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743201b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743202b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743203b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0"));
    List<TransactionReceipt> decodedTransactionReceipts = sszReceiptList.getDecodedList();
    assertEquals(this.transactionReceipts, decodedTransactionReceipts);
  }

  @Test
  public void testSszDecodeEmptyList() {
    SszReceiptList sszReceiptList = new SszReceiptList(List.of());
    List<TransactionReceipt> decodedTransactionReceipts = sszReceiptList.getDecodedList();
    assertTrue(decodedTransactionReceipts.isEmpty());
  }

  @Test
  public void testSszDecodeEmptyListBytes() {
    SszReceiptList sszReceiptList = new SszReceiptList(Bytes.EMPTY);
    List<TransactionReceipt> decodedTransactionReceipts = sszReceiptList.getDecodedList();
    assertTrue(decodedTransactionReceipts.isEmpty());
  }

  @Test
  public void testSszEncode() {
    SszReceiptList sszReceiptList = new SszReceiptList(this.transactionReceipts);
    Bytes encodedTransactionReceipts = sszReceiptList.sszSerialize();
    assertEquals(
        encodedTransactionReceipts,
        Bytes.fromHexString(
            "0x0c000000350100005e020000f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743201b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743202b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0f90126a056570de287d73cd1cb6092bb8fdee6173974955fdef345ae579ee9f475ea743203b9010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000c0"));
  }

  @Test
  public void testSszEncodeEmptyList() {
    SszReceiptList sszReceiptList = new SszReceiptList(List.of());
    Bytes encodedTransactionReceipts = sszReceiptList.sszSerialize();
    assertEquals(encodedTransactionReceipts, Bytes.EMPTY);
  }

  @Test
  public void testSszEncodeEmptyListBytes() {
    SszReceiptList sszReceiptList = new SszReceiptList(Bytes.EMPTY);
    Bytes encodedTransactionReceipts = sszReceiptList.sszSerialize();
    assertEquals(encodedTransactionReceipts, Bytes.EMPTY);
  }
}
