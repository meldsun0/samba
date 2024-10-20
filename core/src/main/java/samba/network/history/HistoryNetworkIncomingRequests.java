package samba.network.history;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.requests.Ping;
import samba.network.NetworkType;

public interface HistoryNetworkIncomingRequests {

    void handlePing(NodeRecord srcNode, Ping ping);

    NetworkType getNetworkType();
}
