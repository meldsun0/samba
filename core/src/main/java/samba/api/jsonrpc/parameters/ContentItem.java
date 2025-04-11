package samba.api.jsonrpc.parameters;

import static com.google.common.base.Preconditions.checkArgument;

public class ContentItem {

  private final String contentKey;
  private final String contentValue;

  public ContentItem(final String contentKey, final String contentValue) {
    checkArgument(contentKey != null, "contentKey cannot be null");
    checkArgument(contentValue != null, "contentValue cannot be null");
    this.contentKey = contentKey;
    this.contentValue = contentValue;
  }

  public static ContentItem build(final String contentKey, final String contentValue) {
    return new ContentItem(contentKey, contentValue);
  }

  public String getContentKey() {
    return contentKey;
  }

  public String getContentValue() {
    return contentValue;
  }
}
