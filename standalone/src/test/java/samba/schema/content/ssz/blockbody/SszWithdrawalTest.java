package samba.schema.content.ssz.blockbody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.GWei;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszWithdrawalTest {

  Withdrawal withdrawal;

  @BeforeEach
  public void setUp() {
    this.withdrawal =
        new Withdrawal(
            UInt64.valueOf(1234),
            UInt64.valueOf(1234),
            Address.wrap(Bytes.fromHexString("0x1234567890123456789012345678901234567890")),
            GWei.of(1234));
  }

  @Test
  public void testSszDecode() {
    SszWithdrawal sszWithdrawal =
        new SszWithdrawal(
            Bytes.fromHexString(
                "0xde8204d28204d29412345678901234567890123456789012345678908204d2"));
    Withdrawal decodedWithdrawal = sszWithdrawal.getDecodedWithdrawal();
    assertTrue(withdrawal.equals(decodedWithdrawal));
  }

  @Test
  public void testSszEncode() {
    SszWithdrawal sszWithdrawal = new SszWithdrawal(withdrawal);
    Bytes encodedWithdrawal = sszWithdrawal.sszSerialize();
    assertEquals(
        encodedWithdrawal,
        Bytes.fromHexString("0xde8204d28204d29412345678901234567890123456789012345678908204d2"));
  }
}
