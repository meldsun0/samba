package samba.api;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.GetContentResult;
import samba.api.jsonrpc.results.PutContentResult;
import samba.api.jsonrpc.results.RecursiveFindNodesResult;
import samba.api.jsonrpc.results.TraceGetContentResult;
import samba.network.history.api.HistoryNetworkInternalAPI;
import samba.network.history.api.methods.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public final class HistoryAPIClient implements HistoryAPI {

  private final HistoryNetworkInternalAPI historyNetworkInternalAPI;

  public HistoryAPIClient(final HistoryNetworkInternalAPI historyNetworkInternalAPI) {
    this.historyNetworkInternalAPI = historyNetworkInternalAPI;
  }

  @Override
  public PutContentResult putContent(final Bytes contentKey, final Bytes contentValue) {
    return PutContent.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
  }

  @Override
  public boolean store(Bytes contentKey, Bytes contentValue) {
    return Store.execute(this.historyNetworkInternalAPI, contentKey, contentValue);
  }

  @Override
  public Optional<String> getEnr(String nodeId) {
    return GetEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public boolean deleteEnr(String nodeId) {
    return DeleteEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public boolean addEnr(String enr) {
    return AddEnr.execute(this.historyNetworkInternalAPI, enr);
  }

  @Override
  public Optional<FindContentResult> findContent(String enr, Bytes contentKey) {
    return FindContent.execute(this.historyNetworkInternalAPI, enr, contentKey);
  }

  @Override
  public List<String> findNodes(String enr, Set<Integer> distances) {
    return FindNodes.execute(this.historyNetworkInternalAPI, enr, distances);
  }

  @Override
  public Optional<String> getLocalContent(Bytes contentKey) {
    return GetLocalContent.execute(this.historyNetworkInternalAPI, contentKey);
  }

  @Override
  public Optional<String> lookupEnr(String nodeId) {
    return LookupEnr.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public Optional<Bytes> offer(String enr, List<Bytes> contents, List<Bytes> contentKeys) {
    return Offer.execute(this.historyNetworkInternalAPI, enr, contents, contentKeys);
  }

  @Override
  public Optional<GetContentResult> getContent(Bytes contentKey) {
    return GetContent.execute(this.historyNetworkInternalAPI, contentKey);
  }

  @Override
  public Optional<TraceGetContentResult> traceGetContent(Bytes contentKey, long startTime) {
    return samba.network.history.api.methods.TraceGetContent.execute(
        this.historyNetworkInternalAPI, startTime, contentKey);
  }

  @Override
  public Optional<RecursiveFindNodesResult> recursiveFindNodes(String nodeId) {
    return RecursiveFindNodes.execute(this.historyNetworkInternalAPI, nodeId);
  }

  @Override
  public Optional<List<List<String>>> getRoutingTable() {
    return GetRoutingTable.execute(this.historyNetworkInternalAPI);
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash) {
    return GetBlockHeaderByBlockHash.execute(this.historyNetworkInternalAPI, blockHash);
  }

  @Override
  public Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash) {
    return GetBlockBodyByBlockHash.execute(this.historyNetworkInternalAPI, blockHash);
  }

  @Override
  public Optional<List<TransactionReceipt>> getReceiptByBlockHash(Hash blockHash) {
    return GetTransactionReceiptByBlockHash.execute(this.historyNetworkInternalAPI, blockHash);
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockNumber(long blockNumber) {
    return GetBlockHeaderByBlockNumber.execute(
        this.historyNetworkInternalAPI, UInt64.fromLongBits(blockNumber));
  }
}
