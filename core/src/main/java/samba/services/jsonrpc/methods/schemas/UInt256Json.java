package samba.services.jsonrpc.methods.schemas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.tuweni.units.bigints.UInt256;

public class UInt256Json extends JsonDeserializer<UInt256> {
  @Override
  public UInt256 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return UInt256.fromHexString(p.getText()); // Assuming JSON provides a hex string
  }
}
