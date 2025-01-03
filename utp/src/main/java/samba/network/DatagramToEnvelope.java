package samba.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.apache.tuweni.bytes.Bytes;
import samba.pipeline.Envelope;
import samba.pipeline.Field;


import java.util.List;

/** UDP Packet -> BytesValue converter with default Netty interface */
public class DatagramToEnvelope extends MessageToMessageDecoder<DatagramPacket> {
  @Override
  protected void decode(ChannelHandlerContext ctx, DatagramPacket msg, List<Object> out) {
    final Envelope envelope = new Envelope();
    final ByteBuf buf = msg.content();
    final byte[] data = new byte[buf.readableBytes()];
    buf.readBytes(data);
    envelope.put(Field.INCOMING, Bytes.wrap(data));
    envelope.put(Field.REMOTE_SENDER, msg.sender());
    out.add(envelope);
  }
}
