package samba.api.jsonrpc.schemas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import samba.api.jsonrpc.results.TraceGetContentResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

public class TraceGetContentResultTest {

  @Test
  public void testTraceGetContentResult() {
    ObjectMapper objectMapper = new ObjectMapper();
    TraceResultResponseItemJson traceResultResponseItemJson =
        new TraceResultResponseItemJson(0, List.of(UInt256.ZERO));
    TraceResultMetadataObjectJson traceResultMetadataObjectJson =
        new TraceResultMetadataObjectJson("enr", UInt256.ZERO);
    Map<UInt256, TraceResultResponseItemJson> responses = new HashMap<>();
    responses.put(UInt256.ZERO, traceResultResponseItemJson);
    Map<UInt256, TraceResultMetadataObjectJson> metadata = new HashMap<>();
    metadata.put(UInt256.ZERO, traceResultMetadataObjectJson);
    TraceResultObjectJson object =
        new TraceResultObjectJson(
            UInt256.ZERO, UInt256.ZERO, responses, metadata, 0, List.of(UInt256.ZERO));
    TraceGetContentResult original = new TraceGetContentResult("0x1234", false, object);
    String jsonString = null;
    try {
      jsonString = objectMapper.writeValueAsString(original);
    } catch (Exception e) {
      fail("Serialization failed: " + e.getMessage());
    }

    TraceGetContentResult deserialized = null;
    try {
      deserialized = objectMapper.readValue(jsonString, TraceGetContentResult.class);
    } catch (Exception e) {
      fail("Deserialization failed: " + e.getMessage());
    }
    assertEquals(original.getContent(), deserialized.getContent());
    assertEquals(original.getUtpTransfer(), deserialized.getUtpTransfer());
    assertEquals(original.getTrace().getOrigin(), deserialized.getTrace().getOrigin());
  }
}
