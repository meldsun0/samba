package samba.domain.content;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.BlockWithReceipts;

/*TODO remove once SSZ decoded is done*/
public class ContentUtil {

  public static Optional<BlockHeader> createBlockHeaderfromSSZBytes(byte[] bytes) {
    // TODO
    return Optional.empty();
  }


  public static Optional<? extends BlockWithReceipts> createBlockWithReceiptsfromSSZBytes(
      byte[] bytes) {
    return Optional.empty();
  }

  public static Optional<BlockBody> createBlockBodyFromSSZBytes(byte[] bytes) {
    return Optional.empty();
  }

  public static Optional<? extends Bytes> createBlockHashFromSSZBytes(byte[] bytes) {
    return Optional.empty();
  }
}
