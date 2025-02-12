package samba.domain.messages.extensions;

import org.apache.tuweni.bytes.Bytes;

public interface PortalExtension {

  int MAX_CLIENT_INFO_BYTE_LENGTH = 200;
  int MAX_CAPABILITIES_LENGTH = 400;
  int MAX_ERROR_BYTE_LENGTH = 300;

  ExtensionType ExtensionType();

  Bytes getSszBytes();

  <T> T getExtension();
}
