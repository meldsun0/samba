package samba.utp;

public enum SessionState {
  SYN_SENT,
  CONNECTED,
  CLOSED,
  SYN_ACKING_FAILED,
  FIN_SEND,
  GOT_FIN,
}
