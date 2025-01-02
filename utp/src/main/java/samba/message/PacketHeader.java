package samba.message;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt32;
import samba.type.Bytes16;


public record PacketHeader (MessageTypeAndVersion typeVersion, Bytes16 connectionId, UInt32 timestampMicroseconds, UInt32 timestampDifferenceMicroseconds, UInt32 windowSize, Bytes16 sequenceNumber, Bytes16 ackNumber) {}



