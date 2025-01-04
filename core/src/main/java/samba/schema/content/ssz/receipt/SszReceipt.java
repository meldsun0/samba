package samba.schema.content.ssz.receipt;

import samba.network.history.HistoryConstants;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszReceipt {

  private static SszByteListSchema<SszByteList> receiptSchema =
      SszByteListSchema.create(HistoryConstants.MAX_RECEIPT_LENGTH);
  private final SszByteList receipt;

  public SszReceipt(SszByteList receipt) {
    this.receipt = receipt;
  }

  public SszReceipt(Bytes receiptBytes) {
    this.receipt = receiptSchema.fromBytes(receiptBytes);
  }

  public SszReceipt(TransactionReceipt receiptReceipt) {
    BytesValueRLPOutput receiptBytes = new BytesValueRLPOutput();
    receiptReceipt.writeToForStorage(receiptBytes, false);
    this.receipt = receiptSchema.fromBytes(receiptBytes.encoded());
  }

  public static SszByteListSchema<SszByteList> getSchema() {
    return receiptSchema;
  }

  public static SszByteList createSszReceipt(TransactionReceipt receiptReceipt) {
    BytesValueRLPOutput receiptBytes = new BytesValueRLPOutput();
    receiptReceipt.writeToForStorage(receiptBytes, false);
    return receiptSchema.fromBytes(receiptBytes.encoded());
  }

  public static TransactionReceipt decodeSszReceipt(SszByteList receipt) {
    BytesValueRLPInput input = new BytesValueRLPInput(receipt.getBytes(), false);
    return TransactionReceipt.readFrom(input);
  }

  public SszByteList getEncodedReceipt() {
    return receipt;
  }

  public TransactionReceipt getDecodedReceipt() {
    BytesValueRLPInput input = new BytesValueRLPInput(receipt.getBytes(), false);
    return TransactionReceipt.readFrom(input);
  }

  public Bytes sszSerialize() {
    return receipt.sszSerialize();
  }
}
