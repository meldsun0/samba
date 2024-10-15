package samba.domain.messages.handler;

import java.util.Optional;

import samba.domain.messages.requests.FindContent;
import samba.domain.messages.PortalWireMessage;

import org.ethereum.beacon.discovery.schema.NodeRecord;

public class FindContentHandler implements PortalWireMessageHandler<FindContent> {

    @Override
    public Optional<PortalWireMessage> handle(NodeRecord srcNode, FindContent findContent) {
        return Optional.empty();
    }
}