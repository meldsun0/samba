package samba.services.jsonrpc.methods.parameters;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.google.common.collect.Lists;

public class ContentDeserializer extends StdDeserializer<List<ContentItem>> {

  public ContentDeserializer() {
    this(null);
  }

  public ContentDeserializer(final Class<?> vc) {
    super(vc);
  }

  @Override
  public List<ContentItem> deserialize(
      final JsonParser jsonparser, final DeserializationContext context) throws IOException {
    final JsonNode contentItemsNode = jsonparser.getCodec().readTree(jsonparser);
    final List<ContentItem> contentItems = Lists.newArrayList();

    for (JsonNode child : contentItemsNode) {
      if (child.isArray() && child.size() == 2) {
        contentItems.add(ContentItem.build(child.get(0).textValue(), child.get(1).textValue()));
      }
    }
    return contentItems;
  }
}
