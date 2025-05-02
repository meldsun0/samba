package samba.api.jsonrpc.schemas.serialization;

import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import org.apache.tuweni.units.bigints.UInt256;

public class UInt256JsonKeyDeserializer extends KeyDeserializer {
  @Override
  public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
    return UInt256.fromHexString(key);
  }
}
