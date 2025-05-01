package samba.api.jsonrpc.schemas.serialization;

import samba.domain.types.unsigned.UInt16;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class UInt16JsonSerializer extends JsonSerializer<UInt16> {
  @Override
  public void serialize(UInt16 value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeNumber(value.getValue());
  }
}
