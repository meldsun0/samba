package samba.network.history.api.methods;

import samba.services.discovery.Discv5Client;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Discv5TalkReq {

  private static final Logger LOG = LoggerFactory.getLogger(Discv5TalkReq.class);

  private final Discv5Client discv5Client;

  public Discv5TalkReq(final Discv5Client discv5Client) {
    this.discv5Client = discv5Client;
  }

  private Optional<String> execute(String enr, String protocolId, String talkReqPayload) {
    try {
      Bytes result =
          this.discv5Client
              .talk(
                  NodeRecordFactory.DEFAULT.fromEnr(enr),
                  Bytes.fromHexString(protocolId),
                  Bytes.fromHexString(talkReqPayload))
              .get();
      return Optional.of(result.toHexString());
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing Discv5FindNodes operation");
      return Optional.empty();
    }
  }

  public static Optional<String> execute(
      final Discv5Client discv5Client, String enr, String protocolId, String talkReqPayload) {
    LOG.debug(
        "Executing Discv5TalkReq with arguments enr: {}, protocolId: {}, talkReqPayload: {}",
        enr,
        protocolId,
        talkReqPayload);
    return new Discv5TalkReq(discv5Client).execute(enr, protocolId, talkReqPayload);
  }
}
