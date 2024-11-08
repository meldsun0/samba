package samba;

import samba.config.SambaConfiguration;

import static org.hyperledger.besu.ethereum.storage.keyvalue.KeyValueSegmentIdentifier.VARIABLES;

public class Samba {

    public static void main(String[] args) throws Exception {
        PortalNode node = new PortalNode(SambaConfiguration.builder().build());
        node.start();
    }
}

//TODO replace NodeRecord.