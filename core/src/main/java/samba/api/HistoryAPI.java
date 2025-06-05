package samba.api;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.GetContentResult;
import samba.api.jsonrpc.results.PutContentResult;
import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.api.jsonrpc.results.TraceGetContentResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;

/**
 * CAUTION: this API is unstable and might be changed in any version in backward incompatible way
 */
public sealed interface HistoryAPI extends SambaAPI permits HistoryAPIClient {

  PutContentResult putContent(final Bytes contentKey, final Bytes contentValue);

  boolean store(final Bytes contentKey, final Bytes contentValue);

  Optional<String> getEnr(final String nodeId);

  boolean deleteEnr(final String nodeId);

  boolean addEnr(final String enr);

  Optional<FindContentResult> findContent(final String enr, final Bytes contentKey);

  List<String> findNodes(final String enr, final Set<Integer> distances);

  Optional<String> getLocalContent(final Bytes contentKey);

  Optional<String> lookupEnr(final String nodeId);

  Optional<Bytes> offer(
      final String enr, final List<Bytes> contents, final List<Bytes> contentKeys);

  Optional<GetContentResult> getContent(final Bytes contentKey);

  Optional<TraceGetContentResult> traceGetContent(final Bytes contentKey, final long startTime);

  Optional<RecursiveFindNodesResult> recursiveFindNodes(final String nodeId);

  Optional<List<List<String>>> getRoutingTable();

  // For Besu

  Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash);

  Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash);

  Optional<List<TransactionReceipt>> getReceiptByBlockHash(Hash blockHash);

  Optional<BlockHeader> getBlockHeaderByBlockNumber(long blockNumber);
}
