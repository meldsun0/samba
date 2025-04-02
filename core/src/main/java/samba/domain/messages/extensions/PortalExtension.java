package samba.domain.messages.extensions;

import samba.domain.types.unsigned.UInt16;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;

public interface PortalExtension {

  int MAX_CLIENT_INFO_BYTE_LENGTH = 200;
  int MAX_CAPABILITIES_LENGTH = 400;
  int MAX_ERROR_BYTE_LENGTH = 300;
  List<UInt16> DEFAULT_CAPABILITIES = List.of(UInt16.ZERO, UInt16.MAX_VALUE);

  ExtensionType ExtensionType();

  Bytes getSszBytes();

  <T> T getExtension();
}
