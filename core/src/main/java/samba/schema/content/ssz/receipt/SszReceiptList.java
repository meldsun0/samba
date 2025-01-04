package samba.schema.content.ssz.receipt;

import samba.network.history.HistoryConstants;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import tech.pegasys.teku.infrastructure.ssz.SszList;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszListSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszReceiptList {

  private static SszListSchema<SszByteList, SszList<SszByteList>> receiptListSchema =
      createByteListListSchema();
  private final SszList<SszByteList> receiptList;

  public SszReceiptList(SszList<SszByteList> receiptList) {
    this.receiptList = receiptList;
  }

  public SszReceiptList(List<TransactionReceipt> receipts) {
    this.receiptList = createSszBytesList(receipts);
  }

  public static SszListSchema<SszByteList, SszList<SszByteList>> getSchema() {
    return receiptListSchema;
  }

  private static SszListSchema<SszByteList, SszList<SszByteList>> createByteListListSchema() {
    SszByteListSchema byteListSchema = SszReceipt.getSchema();
    return SszListSchema.create(byteListSchema, HistoryConstants.MAX_TRANSACTION_COUNT);
  }

  private static SszList<SszByteList> createSszBytesList(List<TransactionReceipt> receipts) {
    List<SszByteList> sszByteLists =
        receipts.stream()
            .map(
                (receipt) -> {
                  BytesValueRLPOutput receiptBytes = new BytesValueRLPOutput();
                  receipt.writeToForStorage(receiptBytes, false);
                  return receiptBytes.encoded();
                })
            .map(SszReceipt.getSchema()::fromBytes)
            .collect(Collectors.toList());
    return (SszList<SszByteList>)
        SszListSchema.create(SszReceipt.getSchema(), HistoryConstants.MAX_TRANSACTION_COUNT)
            .createFromElements(sszByteLists);
  }

  public static SszList<SszByteList> createSszList(List<TransactionReceipt> receipts) {
    return createSszBytesList(receipts);
  }

  public static List<TransactionReceipt> decodeSszList(SszList<SszByteList> receiptList) {
    return receiptList.stream()
        .map((sszByteList) -> SszReceipt.decodeSszReceipt(sszByteList))
        .collect(Collectors.toList());
  }

  public SszList<SszByteList> getEncodedList() {
    return receiptList;
  }

  public List<TransactionReceipt> getDecodedList() {
    return receiptList.stream()
        .map((sszByteList) -> SszReceipt.decodeSszReceipt(sszByteList))
        .collect(Collectors.toList());
  }

  public static SszReceiptList decodeBytes(Bytes bytes) {
    return new SszReceiptList(receiptListSchema.sszDeserialize(bytes));
  }

  public Bytes sszSerialize() {
    return receiptList.sszSerialize();
  }
}
