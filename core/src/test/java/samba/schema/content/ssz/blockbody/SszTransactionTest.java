package samba.schema.content.ssz.blockbody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszTransactionTest {

  Transaction transaction;

  @BeforeEach
  public void setUp() {
    this.transaction =
        Transaction.builder()
            .nonce(1)
            .gasPrice(Wei.of(1))
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
            .build();
  }

  @Test
  public void testSszDecode() {
    SszTransaction sszTransaction =
        new SszTransaction(
            Bytes.fromHexString(
                "0xf839010101941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1"));
    Transaction decodedTransaction = sszTransaction.getTransaction();
    assertTrue(this.transaction.equals(decodedTransaction));
  }

  @Test
  public void testSszEncode() {
    SszTransaction sszTransaction = new SszTransaction(this.transaction);
    Bytes encodedTransaction = sszTransaction.sszSerialize();
    assertEquals(
        encodedTransaction,
        Bytes.fromHexString(
            "0xf839010101941234567890123456789012345678901234567890019412345678901234567890123456789012345678901b84499602d2843ade68b1"));
  }
}
