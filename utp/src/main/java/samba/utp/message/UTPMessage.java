package samba.utp.message;

import samba.utp.data.UtpPacket;

public interface UTPMessage {

  UtpPacket build();
}
