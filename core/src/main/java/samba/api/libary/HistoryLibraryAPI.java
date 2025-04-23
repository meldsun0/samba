package samba.api.libary;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryLibraryAPI {

  PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue);

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

  Optional<String> discv5GetEnr(final String nodeId);
}
