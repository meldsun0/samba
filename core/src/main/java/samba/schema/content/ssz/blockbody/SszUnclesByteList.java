package samba.schema.content.ssz.blockbody;

import samba.network.history.HistoryConstants;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.mainnet.MainnetBlockHeaderFunctions;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import tech.pegasys.teku.infrastructure.ssz.collections.SszByteList;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszByteListSchema;

public class SszUnclesByteList {

  private static SszByteListSchema unclesSchema =
      SszByteListSchema.create(HistoryConstants.MAX_ENCODED_UNCLES_LENGTH);
  private final SszByteList uncles;

  public SszUnclesByteList(SszByteList uncles) {
    this.uncles = uncles;
  }

  public SszUnclesByteList(Bytes unclesBytes) {
    this.uncles = unclesSchema.fromBytes(unclesBytes);
  }

  public SszUnclesByteList(List<BlockHeader> unclesList) {
    BytesValueRLPOutput unclesRLP = new BytesValueRLPOutput();
    unclesRLP.startList();
    unclesList.forEach((uncle) -> uncle.writeTo(unclesRLP));
    unclesRLP.endList();
    this.uncles = unclesSchema.fromBytes(unclesRLP.encoded());
  }

  public static SszByteListSchema getSchema() {
    return unclesSchema;
  }

  public static SszByteList createSszUncles(List<BlockHeader> unclesList) {
    BytesValueRLPOutput unclesRLP = new BytesValueRLPOutput();
    unclesRLP.startList();
    unclesList.forEach((uncle) -> uncle.writeTo(unclesRLP));
    unclesRLP.endList();
    return unclesSchema.fromBytes(unclesRLP.encoded());
  }

  public static List<BlockHeader> decodeSszUncles(SszByteList uncles) {
    BytesValueRLPInput unclesRLP = new BytesValueRLPInput(uncles.getBytes(), false);
    unclesRLP.enterList();
    List<BlockHeader> unclesList =
        unclesRLP.readList(
            (RLPInput in) -> BlockHeader.readFrom(in, new MainnetBlockHeaderFunctions()));
    return unclesList;
  }

  public SszByteList getEncodedUncles() {
    return uncles;
  }

  public List<BlockHeader> getDecodedUncles() {
    BytesValueRLPInput unclesRLP = new BytesValueRLPInput(uncles.getBytes(), false);
    unclesRLP.enterList();
    List<BlockHeader> unclesList =
        unclesRLP.readList(
            (RLPInput in) -> BlockHeader.readFrom(in, new MainnetBlockHeaderFunctions()));
    return unclesList;
  }

  public Bytes sszSerialize() {
    return uncles.sszSerialize();
  }
}
