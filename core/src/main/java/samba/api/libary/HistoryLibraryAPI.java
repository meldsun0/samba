package samba.api.libary;

import samba.api.jsonrpc.results.FindContentResult;
import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;

import java.util.Optional;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryLibraryAPI {

  PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue);

  boolean store(final Bytes contentKey, final Bytes contentValue);

  Optional<String> getEnr(final String nodeId);

  boolean deleteEnr(final String nodeId);

  boolean addEnr(final String enr);

  Optional<FindContentResult> findContent(final String enr, final Bytes contentKey);
}
