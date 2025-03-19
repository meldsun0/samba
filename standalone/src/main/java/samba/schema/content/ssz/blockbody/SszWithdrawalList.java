package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.Withdrawal;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszWithdrawalList {

  private static final SszListSchema<SszByteList, SszList<SszByteList>> withdrawalListSchema =
      createByteListListSchema();
  private final SszList<SszByteList> withdrawalList;

  public SszWithdrawalList(SszList<SszByteList> withdrawalList) {
    this.withdrawalList = withdrawalList;
  }

  public SszWithdrawalList(List<Withdrawal> withdrawals) {
    this.withdrawalList = createSszBytesList(withdrawals);
  }

  public SszWithdrawalList(Bytes bytes) {
    this.withdrawalList = withdrawalListSchema.sszDeserialize(bytes);
  }

  public static SszListSchema<SszByteList, SszList<SszByteList>> getSchema() {
    return withdrawalListSchema;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
    SszByteListSchema byteListSchema = SszWithdrawal.getSchema();
    return SszListSchema.create(byteListSchema, HistoryConstants.MAX_WITHDRAWAL_COUNT);
  }

  private static SszList<SszByteList> createSszBytesList(List<Withdrawal> withdrawals) {
    List<SszByteList> sszByteLists =
        withdrawals.stream()
            .map(
                (withdrawal) -> {
                  BytesValueRLPOutput withdrawalBytes = new BytesValueRLPOutput();
                  withdrawal.writeTo(withdrawalBytes);
                  return withdrawalBytes.encoded();
                })
            .map(SszWithdrawal.getSchema()::fromBytes)
            .collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(SszWithdrawal.getSchema(), HistoryConstants.MAX_WITHDRAWAL_COUNT)
            .createFromElements(sszByteLists);
  }

  public static SszList<SszByteList> createSszList(List<Withdrawal> withdrawals) {
    return createSszBytesList(withdrawals);
  }

  public static List<Withdrawal> decodeSszList(SszList<SszByteList> withdrawalList) {
    return withdrawalList.stream()
        .map((sszByteList) -> SszWithdrawal.decodeSszWithdrawal(sszByteList))
        .collect(Collectors.toList());
  }

  public SszList<SszByteList> getEncodedList() {
    return withdrawalList;
  }

  public List<Withdrawal> getDecodedList() {
    return withdrawalList.stream()
        .map((sszByteList) -> SszWithdrawal.decodeSszWithdrawal(sszByteList))
        .collect(Collectors.toList());
  }

  public static SszWithdrawalList decodeBytes(Bytes bytes) {
    return new SszWithdrawalList(withdrawalListSchema.sszDeserialize(bytes));
  }

  public Bytes sszSerialize() {
    return withdrawalList.sszSerialize();
  }
}
