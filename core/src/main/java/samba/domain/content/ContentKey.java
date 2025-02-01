package samba.domain.content;

import samba.schema.content.ssz.ContentKeyBlockHashContainer;
import samba.schema.content.ssz.ContentKeyBlockNumberContainer;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class ContentKey {

  private final ContentType contentType;
  private final ContentKeyBlockHashContainer hashContainer;
  private final ContentKeyBlockNumberContainer numberContainer;

  public ContentKey(ContentType contentType, ContentKeyBlockHashContainer hashContainer) {
    this.contentType = contentType;
    this.hashContainer = hashContainer;
    this.numberContainer = null;
  }

  public ContentKey(ContentType contentType, ContentKeyBlockNumberContainer numberContainer) {
    this.contentType = contentType;
    this.hashContainer = null;
    this.numberContainer = numberContainer;
  }

  public ContentKey(ContentType contentType, Bytes32 blockHash) {
    this.contentType = contentType;
    this.hashContainer = new ContentKeyBlockHashContainer(blockHash);
    this.numberContainer = null;
  }

  public ContentKey(ContentType contentType, UInt64 blockNumber) {
    this.contentType = contentType;
    this.hashContainer = null;
    this.numberContainer = new ContentKeyBlockNumberContainer(blockNumber);
  }

  public ContentKey(Bytes sszBytes) {
    this.contentType = ContentType.fromContentKey(sszBytes);
    if (contentType == ContentType.BLOCK_HEADER_BY_NUMBER) {
      this.hashContainer = null;
      this.numberContainer = ContentKeyBlockNumberContainer.decodeBytes(sszBytes.slice(1));
    } else {
      this.hashContainer = ContentKeyBlockHashContainer.decodeBytes(sszBytes.slice(1));
      this.numberContainer = null;
    }
  }

  public ContentType getContentType() {
    return contentType;
  }

  public UInt64 getBlockNumber() {
    if (ContentType.BLOCK_HEADER_BY_NUMBER == contentType) {
      return numberContainer.getBlockNumber();
    }
    throw new UnsupportedOperationException("Content type is not BLOCK_HEADER_BY_NUMBER");
  }

  public Bytes getBlockHash() {
    if (ContentType.BLOCK_HEADER_BY_NUMBER != contentType) {
      return hashContainer.getBlockHash();
    }
    throw new UnsupportedOperationException("Content type is not BLOCK_HASH compatible");
  }

  public Bytes getBlockNumberSsz() {
    if (ContentType.BLOCK_HEADER_BY_NUMBER == contentType) {
      return Bytes.concatenate(
          Bytes.of(contentType.getByteValue()), numberContainer.sszSerialize());
    }
    throw new UnsupportedOperationException(
        "Content type is not BLOCK_HEADER_BY_NUMBER compatible");
  }

  public Bytes getBlockHashSsz() {
    if (ContentType.BLOCK_HEADER_BY_NUMBER != contentType) {
      return Bytes.concatenate(Bytes.of(contentType.getByteValue()), hashContainer.sszSerialize());
    }
    throw new UnsupportedOperationException("Content type is not BLOCK_HASH compatible");
  }

  public Bytes getSszBytes() {
    if (ContentType.BLOCK_HEADER_BY_NUMBER == contentType) {
      return Bytes.concatenate(
          Bytes.of(contentType.getByteValue()), numberContainer.sszSerialize());
    }
    return Bytes.concatenate(Bytes.of(contentType.getByteValue()), hashContainer.sszSerialize());
  }

  public static ContentKey decode(Bytes sszContentKey) {
    return new ContentKey(sszContentKey);
  }
}
