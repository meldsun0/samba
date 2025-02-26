package samba.services.utp;

import meldsun0.utp.network.TransportAddress;
import org.ethereum.beacon.discovery.schema.NodeRecord;

public class UTPAddress implements TransportAddress<NodeRecord> {

  private NodeRecord nodeRecord;

  public UTPAddress(NodeRecord nodeRecord) {
    this.nodeRecord = nodeRecord;
  }

  @Override
  public NodeRecord getAddress() {
    return this.nodeRecord;
  }
}
