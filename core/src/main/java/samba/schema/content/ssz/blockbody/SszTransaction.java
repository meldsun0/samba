package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.Transaction;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszTransaction {

  private static SszByteListSchema transactionSchema =
      SszByteListSchema.create(HistoryConstants.MAX_TRANSACTION_LENGTH);
  private final SszByteList transaction;

  public SszTransaction(SszByteList transaction) {
    this.transaction = transaction;
  }

  public SszTransaction(Bytes transactionBytes) {
    this.transaction = transactionSchema.fromBytes(transactionBytes);
  }

  public SszTransaction(Transaction transactionTransaction) {
    this.transaction = transactionSchema.fromBytes(transactionTransaction.encoded());
  }

  public static SszByteListSchema getSchema() {
    return transactionSchema;
  }

  public static SszByteList createSszTransaction(Transaction transactionTransaction) {
    return transactionSchema.fromBytes(transactionTransaction.encoded());
  }

  public static Transaction decodeSszTransaction(SszByteList transaction) {
    return Transaction.readFrom(transaction.getBytes());
  }

  public SszByteList getEncodedTransaction() {
    return transaction;
  }

  public Transaction getTransaction() {
    return Transaction.readFrom(transaction.getBytes());
  }

  public Bytes sszSerialize() {
    return transaction.sszSerialize();
  }
}
