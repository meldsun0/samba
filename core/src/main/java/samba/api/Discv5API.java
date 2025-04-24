package samba.api;

import samba.api.jsonrpc.results.NodeInfo;

import java.net.InetSocketAddress;
import java.util.Optional;

/**
 * CAUTION: this API is unstable and might be changed in any version in backward incompatible way
 */
public interface Discv5API {

    Optional<String> getEnr(final String nodeId);

    Optional<NodeInfo> getNodeInfo();

    Optional<NodeInfo> updateNodeInfo(InetSocketAddress socketAddress, boolean isTCP);
}
