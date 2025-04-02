package samba.services.jsonrpc.methods.schemas;

import samba.domain.types.unsigned.UInt16;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class UInt16JsonDeserializer extends JsonDeserializer<UInt16> {
  @Override
  public UInt16 deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return UInt16.valueOf(p.getIntValue());
  }
}
