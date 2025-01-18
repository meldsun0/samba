package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.Transaction;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszTransactionList {

  private static SszListSchema<SszByteList, SszList<SszByteList>> transactionListSchema =
      createByteListListSchema();
  private final SszList<SszByteList> transactionList;

  public SszTransactionList(SszList<SszByteList> transactionList) {
    this.transactionList = transactionList;
  }

  public SszTransactionList(List<Transaction> transactions) {
    this.transactionList = createSszBytesList(transactions);
  }

  public SszTransactionList(Bytes bytes) {
    this.transactionList = transactionListSchema.sszDeserialize(bytes);
  }

  public static SszListSchema<SszByteList, SszList<SszByteList>> getSchema() {
    return transactionListSchema;
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
    SszByteListSchema byteListSchema = SszTransaction.getSchema();
    return SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT);
  }

  private static SszList<SszByteList> createSszBytesList(List<Transaction> transactions) {
    List<SszByteList> sszByteLists =
        transactions.stream()
            .map((transaction) -> transaction.encoded())
            .map(SszTransaction.getSchema()::fromBytes)
            .collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(SszTransaction.getSchema(), HistoryConstants.MAX_TRANSACTION_COUNT)
            .createFromElements(sszByteLists);
  }

  public static SszList<SszByteList> createSszList(List<Transaction> transactions) {
    return createSszBytesList(transactions);
  }

  public static List<Transaction> decodeSszList(SszList<SszByteList> transactionList) {
    return transactionList.stream()
        .map((sszByteList) -> SszTransaction.decodeSszTransaction(sszByteList))
        .collect(Collectors.toList());
  }

  public SszList<SszByteList> getEncodedList() {
    return transactionList;
  }

  public List<Transaction> getDecodedList() {
    return transactionList.stream()
        .map((sszByteList) -> SszTransaction.decodeSszTransaction(sszByteList))
        .collect(Collectors.toList());
  }

  public static SszTransactionList decodeBytes(Bytes bytes) {
    return new SszTransactionList(transactionListSchema.sszDeserialize(bytes));
  }

  public Bytes sszSerialize() {
    return transactionList.sszSerialize();
  }
}
