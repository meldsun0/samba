package samba.services.jsonrpc.methods.schemas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UInt256JsonDeserializer extends JsonDeserializer<UInt256> {
  Logger LOG = LoggerFactory.getLogger(UInt256JsonDeserializer.class.getName());

  @Override
  public UInt256 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return UInt256.fromHexString(p.getText());
  }
}
