package samba.schema.primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;

import samba.domain.types.unsigned.UInt16;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.infrastructure.ssz.collections.SszPrimitiveList;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchema;
import tech.pegasys.teku.infrastructure.ssz.schema.collections.SszPrimitiveListSchema;

public class SszUInt16Test {

  @SuppressWarnings("unchecked")
  @Test
  public void testSszRoundTrip() {
    SszUInt16 sszUInt16 = SszUInt16.of(UInt16.valueOf(0x1234));
    UInt16 value = sszUInt16.get();
    SszPrimitiveSchema<UInt16, SszUInt16> schema =
        (SszPrimitiveSchema<UInt16, SszUInt16>) sszUInt16.getSchema();
    SszUInt16 sszUInt16decoded = schema.boxed(value);

    assertEquals(sszUInt16.sszSerialize(), sszUInt16decoded.sszSerialize());
    assertEquals(sszUInt16.get(), sszUInt16decoded.get());
  }

  @Test
  public void testSszCollectionDecode() {
    SszPrimitiveList<UInt16, SszUInt16> sszUInt16List =
        SszPrimitiveListSchema.create(SszUInt16.UINT16_SCHEMA, 10)
            .sszDeserialize(Bytes.fromHexString("0x01000200"));
    assertEquals(
        sszUInt16List.stream().map(SszUInt16::get).toList(),
        List.of(UInt16.valueOf(1), UInt16.valueOf(2)));
  }

  @Test
  public void testSszCollectionEncode() {
    List<UInt16> values = List.of(UInt16.valueOf(1), UInt16.valueOf(2));
    SszPrimitiveList<UInt16, SszUInt16> sszUInt16List =
        SszPrimitiveListSchema.create(SszUInt16.UINT16_SCHEMA, 10).of(values);
    Bytes encodedBytes = sszUInt16List.sszSerialize();
    assertEquals(encodedBytes, Bytes.fromHexString("0x01000200"));
  }
}
