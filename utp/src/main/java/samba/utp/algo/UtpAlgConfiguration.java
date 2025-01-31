package samba.utp.algo;

public class UtpAlgConfiguration {

  public static final int MAX_CONNECTION_ATTEMPTS = 5;
  public static final int CONNECTION_ATTEMPT_INTERVALL_MILLIS = 5000;
  public static long MINIMUM_DELTA_TO_MAX_WINDOW_MICROS = 1000000;
  // ack every second packets
  public static int SKIP_PACKETS_UNTIL_ACK = 2;

  /**
   * Auto ack every packet that is smaller than ACK_NR from ack packet. Some Implementations like
   * libutp do this.
   */
  public static boolean AUTO_ACK_SMALLER_THAN_ACK_NUMBER = true;

  /** if oldest mindelay sample is older than that, update it. */
  public static long MINIMUM_DIFFERENCE_TIMESTAMP_MICROSEC = 120000000L;

  public static int MINIMUM_TIMEOUT_MILLIS = 500;
  public static PacketSizeModus PACKET_SIZE_MODE = PacketSizeModus.CONSTANT_1472;

  /** maximum packet size should be dynamically set once path mtu discovery implemented. */
  public static volatile int MAX_PACKET_SIZE = 1472;

  public static volatile int MIN_PACKET_SIZE = 150;
  public static volatile int MINIMUM_MTU = 576;

  /** Maximal window increase per RTT - increase to allow uTP throttle up faster. */
  public static volatile int MAX_CWND_INCREASE_PACKETS_PER_RTT = 3000;

  /** maximal buffering delay */
  public static volatile int C_CONTROL_TARGET_MICROS = 100000;

  /** activate burst sending */
  public static volatile boolean SEND_IN_BURST = true;

  /** Reduce burst sending artificially */
  public static volatile int MAX_BURST_SEND = 5;

  /** Minimum number of acks past seqNr=x to trigger a resend of seqNr=x; */
  public static volatile int MIN_SKIP_PACKET_BEFORE_RESEND = 3;

  public static volatile long MICROSECOND_WAIT_BETWEEN_BURSTS = 28000;
  public static volatile long TIME_WAIT_AFTER_LAST_PACKET = 3000000;
  public static volatile boolean ONLY_POSITIVE_GAIN = false;
  public static volatile boolean DEBUG = false;

  public static String getString() {
    return String.format(
        "MINIMUM_TIMEOUT_MILLIS: %d, PACKET_SIZE_MODE: %s, MAX_PACKET_SIZE: %d, MIN_PACKET_SIZE: %d, "
            + "MINIMUM_MTU: %d, MAX_CWND_INCREASE_PACKETS_PER_RTT: %d, C_CONTROL_TARGET_MICROS: %d, "
            + "SEND_IN_BURST: %b, MAX_BURST_SEND: %d, MIN_SKIP_PACKET_BEFORE_RESEND: %d, "
            + "MICROSECOND_WAIT_BETWEEN_BURSTS: %d, TIME_WAIT_AFTER_LAST_PACKET: %d, ONLY_POSITIVE_GAIN: %b, DEBUG: %b",
        MINIMUM_TIMEOUT_MILLIS,
        PACKET_SIZE_MODE,
        MAX_PACKET_SIZE,
        MIN_PACKET_SIZE,
        MINIMUM_MTU,
        MAX_CWND_INCREASE_PACKETS_PER_RTT,
        C_CONTROL_TARGET_MICROS,
        SEND_IN_BURST,
        MAX_BURST_SEND,
        MIN_SKIP_PACKET_BEFORE_RESEND,
        MICROSECOND_WAIT_BETWEEN_BURSTS,
        TIME_WAIT_AFTER_LAST_PACKET,
        ONLY_POSITIVE_GAIN,
        DEBUG);
  }
}
