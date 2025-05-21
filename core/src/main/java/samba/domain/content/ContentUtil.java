package samba.domain.content;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ContentUtil {

  public static Optional<ContentKey> createContentKeyFromSszBytes(Bytes contentkeyBytes) {
    Optional<ContentKey> contentKey = Optional.of(new ContentKey(contentkeyBytes));
    return contentKey;
  }

  public static Optional<ContentBlockHeader> createBlockHeaderfromSszBytes(Bytes blockHeaderBytes) {
    Optional<ContentBlockHeader> blockHeader =
        Optional.of(new ContentBlockHeader(blockHeaderBytes));
    return blockHeader;
  }

  public static Optional<ContentBlockBody> createBlockBodyFromSszBytes(Bytes blockBodyBytes) {
    Optional<ContentBlockBody> blockBody = Optional.of(new ContentBlockBody(blockBodyBytes));
    return blockBody;
  }

  public static Optional<ContentReceipts> createReceiptsFromSszBytes(Bytes receiptsBytes) {
    Optional<ContentReceipts> receipts = Optional.of(new ContentReceipts(receiptsBytes));
    return receipts;
  }

  public static Bytes createBlockNumberInSSZ(final ContentBlockHeader contentBlockHeader) {
    return new ContentKey(
            ContentType.BLOCK_HEADER_BY_NUMBER,
            UInt64.valueOf(contentBlockHeader.getBlockHeader().getNumber()))
        .getBlockNumberSsz();
  }

  public static Bytes createBlockHashKey(final ContentBlockHeader blockHeader) {
    return new ContentKey(ContentType.BLOCK_HEADER, blockHeader.getBlockHeader().getHash().copy())
        .getBlockHashSsz();
  }
}
