package samba.api.jsonrpc.parameters;

import static java.util.Collections.emptyList;

import samba.domain.messages.PortalWireMessage;

import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.tuweni.bytes.Bytes;

public class ContentItemsParameter {

  private List<ContentItem> contentItems;

  @JsonCreator
  public ContentItemsParameter(
      @JsonDeserialize(using = ContentDeserializer.class) final List<ContentItem> contentItems) {
    this.contentItems = contentItems != null ? contentItems : emptyList();
  }

  public List<Bytes> getContentKeys() {
    return contentItems.stream()
        .map(ContentItem::getContentKey)
        .map(Bytes::fromHexString)
        .collect(Collectors.toList());
  }

  public List<Bytes> getContentValues() {
    return contentItems.stream()
        .map(ContentItem::getContentValue)
        .map(Bytes::fromHexString)
        .collect(Collectors.toList());
  }

  public boolean isNotValid() {
    return (this.contentItems.isEmpty()
        || this.contentItems.size() >= PortalWireMessage.MAX_KEYS
        || this.getContentKeys().size() != this.getContentValues().size());
  }
}
