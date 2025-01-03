package samba.message;

import org.apache.tuweni.units.bigints.UInt32;
import samba.packet.MessageTypeAndVersion;
import samba.type.Bytes16;


public record PacketHeader (MessageTypeAndVersion typeVersion, Bytes16 connectionId, UInt32 timestampMicroseconds, UInt32 timestampDifferenceMicroseconds, UInt32 windowSize, Bytes16 sequenceNumber, Bytes16 ackNumber) {

    int getLength(){
        return UTPMessage.HEADER_BYTES_LENGTH;
    }
}



