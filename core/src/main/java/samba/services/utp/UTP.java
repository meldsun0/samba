package samba.services.utp;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.storage.ContentSaver;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

public interface UTP {

  SafeFuture<?> sendContent(int connectionId, Bytes content);

  SafeFuture<Boolean> getContent(int connectionId, NodeRecord nodeRecord, ContentSaver contentSaver);
}
