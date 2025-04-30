package samba.api;

import samba.api.jsonrpc.results.NodeInfo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * CAUTION: this API is unstable and might be changed in any version in backward incompatible way
 */
public interface Discv5API {

  Optional<String> getEnr(final String nodeId);

  Optional<NodeInfo> getNodeInfo();

  Optional<NodeInfo> updateNodeInfo(InetSocketAddress socketAddress, boolean isTCP);

  Optional<List<String>> findNodes(String enr, Set<Integer> distances);

  Optional<String> talk(final String enr, final String protocolId, final String talkReqPayload);

  Optional<List<List<String>>> getRoutingTable();
}
