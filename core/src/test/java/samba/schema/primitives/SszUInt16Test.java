package samba.schema.primitives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import samba.domain.types.unsigned.UInt16;
import tech.pegasys.teku.infrastructure.ssz.schema.SszPrimitiveSchema;

public class SszUInt16Test {

    @SuppressWarnings("unchecked")
    @Test
    void testSszRoundTrip() {
        SszUInt16 sszUInt16 = SszUInt16.of(UInt16.valueOf(0x1234));
        UInt16 value = sszUInt16.get();
        SszPrimitiveSchema<UInt16, SszUInt16> schema = (SszPrimitiveSchema<UInt16, SszUInt16>) sszUInt16.getSchema();
        SszUInt16 sszUInt16decoded = schema.boxed(value);

        assertEquals(sszUInt16.sszSerialize(), sszUInt16decoded.sszSerialize());
        assertEquals(sszUInt16.get(), sszUInt16decoded.get());

    }
}