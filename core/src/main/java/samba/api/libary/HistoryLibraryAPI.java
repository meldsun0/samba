package samba.api.libary;

import samba.api.jsonrpc.results.PutContentResult;
import samba.domain.content.ContentKey;

import org.apache.tuweni.bytes.Bytes;

public interface HistoryLibraryAPI {

  PutContentResult putContent(final ContentKey contentKey, final Bytes contentValue);
}
