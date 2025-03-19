package samba.domain.messages;

import org.apache.tuweni.bytes.Bytes;

public interface PortalWireMessage {

  int MAX_CUSTOM_PAYLOAD_BYTES = 2048;
  int MAX_DISCV5_PACKET_PAYLOAD_BYTES = 1177;
  int MAX_EXTENSION_PAYLOAD_BYTES = 1100;
  int MAX_DISTANCES = 256;
  int MAX_ENRS = 32;
  int MAX_KEYS = 64;

  MessageType getMessageType();

  Bytes getSszBytes();

  <T> T getMessage();
}
