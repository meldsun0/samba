package samba.services.utp;

import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.storage.ContentSaver;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class UTPService extends Service  implements  UTP {

    private Discv

    @Override
    protected SafeFuture<?> doStart() {

    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }

    @Override
    public SafeFuture<?> sendContent(int connectionId, Bytes content) {
        return null;
    }

    @Override
    public SafeFuture<Boolean> getContent(int connectionId, NodeRecord nodeRecord, ContentSaver contentSaver) {
        //create client thread
        //associate connectionId,
        //collect bytes

        //contentSaver.saveContent(Bytes.EMPTY , Bytes.EMPTY);
        return null;
    }
}
