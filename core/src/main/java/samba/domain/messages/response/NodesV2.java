package samba.domain.messages.response;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import samba.domain.messages.PortalWireMessage;

import java.util.List;

public interface NodesV2 extends PortalWireMessage {



    public int getTotalNumberOfNodes();

    public boolean isNodeListEmpty();

    public List<NodeRecord> getNodes();


}
