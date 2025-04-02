package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;

public interface PortalWireMessage {

  int MAX_CUSTOM_PAYLOAD_BYTES =
      1165; // 1177 bytes is the maximum size of a custom payload in a DISCv5 packet. Preliminary
  // spec value is 1165
  int MAX_EXTENSION_PAYLOAD_BYTES = 1100;
  int MAX_DISTANCES = 256;
  int MAX_ENRS = 32;
  int MAX_KEYS = 64;

  MessageType getMessageType();

  Bytes getSszBytes();

  <T> T getMessage();
}
