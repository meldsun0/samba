package samba.schema.content.ssz.blockbody;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.GWei;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SszWithdrawalListTest {

  List<Withdrawal> withdrawals;

  @BeforeEach
  public void setUp() {
    this.withdrawals = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      this.withdrawals.add(
          new Withdrawal(
              UInt64.valueOf(1234),
              UInt64.valueOf(1234),
              Address.wrap(Bytes.fromHexString("0x1234567890123456789012345678901234567890")),
              GWei.of(1234)));
    }
  }

  @Test
  public void testSszDecode() {
    SszWithdrawalList sszWithdrawalList =
        new SszWithdrawalList(
            Bytes.fromHexString(
                "0x1400000033000000520000007100000090000000de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2"));
    List<Withdrawal> decodedWithdrawals = sszWithdrawalList.getDecodedList();
    for (int i = 0; i < 5; i++) {
      assertTrue(this.withdrawals.get(i).equals(decodedWithdrawals.get(i)));
    }
  }

  @Test
  public void testSszDecodeEmptyList() {
    SszWithdrawalList sszWithdrawalList = new SszWithdrawalList(List.of());
    List<Withdrawal> decodedWithdrawals = sszWithdrawalList.getDecodedList();
    assertTrue(decodedWithdrawals.isEmpty());
  }

  @Test
  public void testSszDecodeEmptyBytes() {
    SszWithdrawalList sszWithdrawalList = new SszWithdrawalList(Bytes.EMPTY);
    List<Withdrawal> decodedWithdrawals = sszWithdrawalList.getDecodedList();
    assertTrue(decodedWithdrawals.isEmpty());
  }

  @Test
  public void testSszEncode() {
    SszWithdrawalList sszWithdrawalList = new SszWithdrawalList(this.withdrawals);
    Bytes encodedWithdrawals = sszWithdrawalList.sszSerialize();
    assertEquals(
        encodedWithdrawals,
        Bytes.fromHexString(
            "0x1400000033000000520000007100000090000000de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2de8204d28204d29412345678901234567890123456789012345678908204d2"));
  }

  @Test
  public void testSszEncodeEmptyList() {
    SszWithdrawalList sszWithdrawalList = new SszWithdrawalList(List.of());
    Bytes encodedWithdrawals = sszWithdrawalList.sszSerialize();
    assertEquals(encodedWithdrawals, Bytes.EMPTY);
  }

  @Test
  public void testSszEncodeEmptyBytes() {
    SszWithdrawalList sszWithdrawalList = new SszWithdrawalList(Bytes.EMPTY);
    Bytes encodedWithdrawals = sszWithdrawalList.sszSerialize();
    assertEquals(encodedWithdrawals, Bytes.EMPTY);
  }
}
