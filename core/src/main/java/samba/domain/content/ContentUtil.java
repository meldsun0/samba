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

  public static boolean isBlockBodyValid(BlockHeader blockHeader, Bytes blockBody) {
    // TODO given a BlockHeader we should validate that value that is a sszbytes of a blockBody
    /*
    Compare header timestamp against SHANGHAI_TIMESTAMP to determine what encoding scheme the block body uses.
    Decode the block body using either pre-shanghai or post-shanghai encoding.
    Validate the decoded block body against the roots in the header.*/
    return true;
  }

  public static boolean isBlockHeaderValid(Bytes blockHash, Bytes blockHeader) {
    // TODO validate blockHeader.
    return true;
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
