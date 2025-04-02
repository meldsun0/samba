package samba.services.jsonrpc.methods.schemas;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.types.unsigned.UInt16;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

public class ClientInfoAndCapabilitiesJsonTest {

  @Test
  public void testClientInfoAndCapabilitiesJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ClientInfoAndCapabilitiesJson clientInfo =
        new ClientInfoAndCapabilitiesJson(
            "TestClient",
            UInt256.fromHexString(
                "0x1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef"),
            List.of(UInt16.valueOf(1), UInt16.valueOf(2)));
    String jsonString = null;
    try {
      jsonString = objectMapper.writeValueAsString(clientInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }

    ClientInfoAndCapabilitiesJson deserializedClientInfo = null;
    try {
      deserializedClientInfo =
          objectMapper.readValue(jsonString, ClientInfoAndCapabilitiesJson.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    assertEquals(clientInfo.getClientInfo(), deserializedClientInfo.getClientInfo());
    assertEquals(clientInfo.getDataRadius(), deserializedClientInfo.getDataRadius());
    assertEquals(clientInfo.getCapabilities(), deserializedClientInfo.getCapabilities());
  }
}
