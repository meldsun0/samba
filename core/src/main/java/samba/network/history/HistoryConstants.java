package samba.network.history;

public class HistoryConstants {

  public static final long MAX_TRANSACTION_LENGTH = 1 << 24; // 2^24
  public static final long MAX_TRANSACTION_COUNT = 1 << 14; // 2^14
  public static final long MAX_RECEIPT_LENGTH = 1 << 27; // 2^27
  public static final long MAX_HEADER_LENGTH = 1 << 11; // 2^11
  public static final long MAX_ENCODED_UNCLES_LENGTH =
      MAX_HEADER_LENGTH * (1 << 4); // MAX_HEADER_LENGTH * 2^4
  public static final long MAX_WITHDRAWAL_COUNT = 16;
  public static final long WITHDRAWAL_LENGTH = 64;
  public static final long MAX_WITHDRAWAL_LENGTH = MAX_WITHDRAWAL_COUNT * WITHDRAWAL_LENGTH;
  public static final long MERGE_TIMESTAMP = 1663224162;
  public static final long SHANGHAI_TIMESTAMP = 1681338455;
  public static final long MAX_EPHEMERAL_HEADER_PAYLOAD = 256;
  public static final long MAX_HEADER_PROOF_LENGTH = 1024;
}
