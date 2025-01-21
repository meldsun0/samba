package samba.domain.messages.utp;

import static com.google.common.base.Preconditions.checkArgument;

import samba.domain.messages.IncomingRequestHandler;
import samba.network.NetworkType;
import samba.services.utp.UTPService;

import java.util.concurrent.CompletableFuture;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UTPNetworkIncomingRequestHandler implements IncomingRequestHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UTPNetworkIncomingRequestHandler.class);

  private UTPService utpService;

  public UTPNetworkIncomingRequestHandler(UTPService utpService) {
    this.utpService = utpService;
  }

  @Override
  public CompletableFuture<Bytes> talk(NodeRecord srcNode, Bytes protocol, Bytes request) {
    checkArgument(
        this.getNetworkType().isEquals(protocol),
        "TALKKREQ message is not from the {}",
        this.getNetworkType().getName());
    // TODO

    return CompletableFuture.completedFuture(null);
  }

  @Override
  public NetworkType getNetworkType() {
    return NetworkType.UTP;
  }
}
