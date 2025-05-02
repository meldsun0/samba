package samba.api.jsonrpc.schemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

public class TraceResultObjectJsonTest {

  @Test
  public void testTraceResultObjectJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    TraceResultResponseItemJson traceResultResponseItemJson =
        new TraceResultResponseItemJson(0, List.of(UInt256.ZERO));
    TraceResultMetadataObjectJson traceResultMetadataObjectJson =
        new TraceResultMetadataObjectJson("enr", UInt256.ZERO);
    Map<UInt256, TraceResultResponseItemJson> responses = new HashMap<>();
    responses.put(UInt256.ZERO, traceResultResponseItemJson);
    Map<UInt256, TraceResultMetadataObjectJson> metadata = new HashMap<>();
    metadata.put(UInt256.ZERO, traceResultMetadataObjectJson);
    TraceResultObjectJson original =
        new TraceResultObjectJson(
            UInt256.ZERO,
            UInt256.ZERO,
            UInt256.ZERO,
            responses,
            metadata,
            0,
            List.of(UInt256.ZERO));
    String jsonString = null;
    try {
      jsonString = objectMapper.writeValueAsString(original);
    } catch (Exception e) {
      fail("Serialization failed: " + e.getMessage());
    }

    TraceResultObjectJson deserialized = null;
    try {
      deserialized = objectMapper.readValue(jsonString, TraceResultObjectJson.class);
    } catch (Exception e) {
      fail("Deserialization failed: " + e.getMessage());
    }
    assertEquals(original.getOrigin(), deserialized.getOrigin());
    assertEquals(original.getTargetId(), deserialized.getTargetId());
    assertEquals(original.getRecievedFrom(), deserialized.getRecievedFrom());
    assertEquals(original.getStartedAtMs(), deserialized.getStartedAtMs());
    assertEquals(original.getCancelled(), deserialized.getCancelled());

    assertEquals(original.getResponses().keySet(), deserialized.getResponses().keySet());
    for (UInt256 k : original.getResponses().keySet()) {
      TraceResultResponseItemJson originalItem = original.getResponses().get(k);
      TraceResultResponseItemJson deserializedItem = deserialized.getResponses().get(k);
      assertNotNull(deserializedItem);
      assertEquals(originalItem.getDurationMs(), deserializedItem.getDurationMs());
      assertEquals(originalItem.getRespondedWith(), deserializedItem.getRespondedWith());
    }

    assertEquals(original.getMetadata().keySet(), deserialized.getMetadata().keySet());
    for (UInt256 k : original.getMetadata().keySet()) {
      TraceResultMetadataObjectJson originalItem = original.getMetadata().get(k);
      TraceResultMetadataObjectJson deserializedItem = deserialized.getMetadata().get(k);
      assertNotNull(deserializedItem);
      assertEquals(originalItem.getEnr(), deserializedItem.getEnr());
      assertEquals(originalItem.getDistance(), deserializedItem.getDistance());
    }
  }
}
