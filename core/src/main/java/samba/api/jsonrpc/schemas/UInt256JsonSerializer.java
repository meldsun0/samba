package samba.api.jsonrpc.schemas;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.tuweni.units.bigints.UInt256;

public class UInt256JsonSerializer extends JsonSerializer<UInt256> {
  @Override
  public void serialize(UInt256 value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.toHexString());
  }
}
