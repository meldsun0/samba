package samba.schema.content.ssz.blockbody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszTransactionListTest {

  List<Transaction> transactions;

  @BeforeEach
  public void setup() {
    this.transactions = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      this.transactions.add(
          Transaction.builder()
              .nonce(1)
              .gasPrice(Wei.of(i + 1))
              .gasLimit(1)
              .to(Address.wrap(Bytes.fromHexString("0x1234567890123456789012345678901234567890")))
              .value(Wei.of(1))
              .payload(Bytes.fromHexString("0x1234567890123456789012345678901234567890"))
              .signature(
                  SECPSignature.create(
                      new BigInteger("1234567890"),
                      new BigInteger("987654321"),
                      (byte) 0,
                      new BigInteger(
                          "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16)))
              .build());
    }
  }

  @Test
  public void testSszDecode() {
    SszTransactionList sszTransactionList =
        new SszTransactionList(
            Bytes.fromHexString(
                "0x140000004f0000008a000000c500000000010000f839010101941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010201941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010301941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010401941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010501941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1"));
    List<Transaction> decodedTransactions = sszTransactionList.getDecodedList();
    for (int i = 0; i < 5; i++) {
      assertTrue(this.transactions.get(i).equals(decodedTransactions.get(i)));
    }
  }

  @Test
  public void testSszDecodeEmptyList() {
    SszTransactionList sszTransactionList = new SszTransactionList(List.of());
    List<Transaction> decodedTransactions = sszTransactionList.getDecodedList();
    assertTrue(decodedTransactions.isEmpty());
  }

  @Test
  public void testSszDecodeEmptyBytes() {
    SszTransactionList sszTransactionList = new SszTransactionList(Bytes.EMPTY);
    List<Transaction> decodedTransactions = sszTransactionList.getDecodedList();
    assertTrue(decodedTransactions.isEmpty());
  }

  @Test
  public void testSszEncode() {
    SszTransactionList sszTransactionList = new SszTransactionList(this.transactions);
    Bytes encodedTransactions = sszTransactionList.sszSerialize();
    assertEquals(
        encodedTransactions,
        Bytes.fromHexString(
            "0x140000004f0000008a000000c500000000010000f839010101941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010201941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010301941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010401941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1f839010501941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1"));
  }

  @Test
  public void testSszEncodeEmptyList() {
    SszTransactionList sszTransactionList = new SszTransactionList(List.of());
    Bytes encodedTransactions = sszTransactionList.sszSerialize();
    assertEquals(encodedTransactions, Bytes.EMPTY);
  }

  @Test
  public void testSszEncodeEmptyBytes() {
    SszTransactionList sszTransactionList = new SszTransactionList(Bytes.EMPTY);
    Bytes encodedTransactions = sszTransactionList.sszSerialize();
    assertEquals(encodedTransactions, Bytes.EMPTY);
  }
}
