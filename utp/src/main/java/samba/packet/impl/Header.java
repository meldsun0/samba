package samba.packet.impl;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt32;
import samba.packet.MessageType;
import samba.util.DecodeException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Header extends AbstractBytes  {

  private static final int TYPE_VERSION_LENGTH = 1;
  private static final int EXTENSION_LENGTH = 1;
  private static final int CONNECTION_ID_LENGTH = 2;
  private static final int TIMESTAMP_MICROSECONDS_LENGTH = 4;
  private static final int TIMESTAMP_DIFFERENCE_MICROSECONDS_LENGTH = 4;
  private static final int WINDOW_SIZE_LENGTH = 4;
  private static final int ACK_NR_LENGTH = 2;
  private static final int SEQUENCE_NR_LENGTH = 2;
  private static final int HEADER_BYTES_LENGTH = 20;
  private static final Bytes VERSION = Bytes.fromHexString("0x01");

  private final MessageType type;
  private final Bytes version = VERSION;
  private final Bytes connectionId;
  private final UInt32 timestampMicroseconds;
  private final UInt32 timestampDifferenceMicroseconds;
  private final UInt32 windowSize;
  private final Bytes sequenceNumber;
  private final Bytes ackNumber;


  public Bytes getHeaderBytes() {
    return Bytes.concatenate(Bytes.of(this.type.getByteValue()).or(this.version),
                    this.connectionId,
                    this.timestampMicroseconds.toBytes(),
                    this.timestampDifferenceMicroseconds.toBytes(),
                    this.windowSize.toBytes(),
                    this.sequenceNumber,
                    this.ackNumber);
  }

  public Header(MessageType type, Bytes connectionId, UInt32 timestampMicroseconds, UInt32 timestampDifferenceMicroseconds, UInt32 windowSize, Bytes sequenceNumber, Bytes ackNumber) {
    super(Bytes.EMPTY);
    this.type = type;
    this.connectionId = connectionId;
    this.timestampMicroseconds = timestampMicroseconds;
    this.timestampDifferenceMicroseconds = timestampDifferenceMicroseconds;
    this.windowSize = windowSize;
    this.sequenceNumber = sequenceNumber;
    this.ackNumber = ackNumber;
  }

  public int getSize() {
    return getHeaderBytes().size();
  }

  @Override
  public String toString() {
    return "Header{" +
            "type=" + this.type+ ", " +
            "connectionId=" + this.connectionId.toString() +
            "timestampMicroseconds=" + this.timestampMicroseconds.toString() +
            "timestampDifferenceMicroseconds=" + this.timestampDifferenceMicroseconds.toString() +
            "windowSize=" + this.windowSize.toString() +
            "sequenceNumber=" + this.sequenceNumber.toString() +
            "ackNumber=" + this.ackNumber.toString()
            + "}";
  }

  @Override
  public void validate() throws DecodeException {
    checkNotNull(this.type, "Type");
    checkNotNull(this.version, "Version");
    checkNotNull(this.connectionId, "ConnectionId");
    checkNotNull(this.timestampMicroseconds, "TimestampMicroseconds");
    checkNotNull(this.timestampDifferenceMicroseconds, "TimestampDifferenceMicroseconds");
    checkNotNull(this.windowSize, "WindowSize");
    checkNotNull(this.sequenceNumber, "SequenceNumber");
    checkNotNull(this.ackNumber, "ackNumber");

    checkArgument(Bytes.of(this.type.getByteValue()).or(this.version).size() == TYPE_VERSION_LENGTH, "Type and Version should be of length "+ TYPE_VERSION_LENGTH);
    checkArgument(this.connectionId.size() == CONNECTION_ID_LENGTH, "ConnectionId should be of length "+ CONNECTION_ID_LENGTH);
    checkArgument(this.timestampMicroseconds.toBytes().size() == TIMESTAMP_MICROSECONDS_LENGTH, "TimestampMicroseconds should be of length "+TIMESTAMP_MICROSECONDS_LENGTH);
    checkArgument(this.timestampDifferenceMicroseconds.toBytes().size() == TIMESTAMP_DIFFERENCE_MICROSECONDS_LENGTH, "TimestampDifferenceMicroseconds should be of length "+TIMESTAMP_DIFFERENCE_MICROSECONDS_LENGTH);
    checkArgument(this.windowSize.toBytes().size() == WINDOW_SIZE_LENGTH, "WindowSize should be of length "+WINDOW_SIZE_LENGTH);
    checkArgument(this.sequenceNumber.size() ==  SEQUENCE_NR_LENGTH, "SequenceNumber should be of length "+ SEQUENCE_NR_LENGTH);
    checkArgument(this.ackNumber.size() == ACK_NR_LENGTH, "ackNumber should be of length "+ ACK_NR_LENGTH);
  }


}
