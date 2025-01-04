package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszWithdrawal {

  private static SszByteListSchema<SszByteList> withdrawalSchema =
      SszByteListSchema.create(HistoryConstants.MAX_WITHDRAWAL_LENGTH);
  private final SszByteList withdrawal;

  public SszWithdrawal(SszByteList withdrawal) {
    this.withdrawal = withdrawal;
  }

  public SszWithdrawal(Bytes withdrawalBytes) {
    this.withdrawal = withdrawalSchema.fromBytes(withdrawalBytes);
  }

  public SszWithdrawal(Withdrawal withdrawalWithdrawal) {
    BytesValueRLPOutput withdrawalBytes = new BytesValueRLPOutput();
    withdrawalWithdrawal.writeTo(withdrawalBytes);
    this.withdrawal = withdrawalSchema.fromBytes(withdrawalBytes.encoded());
  }

  public static SszByteListSchema<SszByteList> getSchema() {
    return withdrawalSchema;
  }

  public static SszByteList createSszWithdrawal(Withdrawal withdrawalWithdrawal) {
    BytesValueRLPOutput withdrawalBytes = new BytesValueRLPOutput();
    withdrawalWithdrawal.writeTo(withdrawalBytes);
    return withdrawalSchema.fromBytes(withdrawalBytes.encoded());
  }

  public static Withdrawal decodeSszWithdrawal(SszByteList withdrawal) {
    return Withdrawal.readFrom(withdrawal.getBytes());
  }

  public SszByteList getEncodedWithdrawal() {
    return withdrawal;
  }

  public Withdrawal getDecodedWithdrawal() {
    return Withdrawal.readFrom(withdrawal.getBytes());
  }

  public Bytes sszSerialize() {
    return withdrawal.sszSerialize();
  }
}
