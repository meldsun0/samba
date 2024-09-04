package samba;

import samba.config.SambaConfiguration;

public class Samba {

    public static void main(String[] args) throws Exception {

        PortalNode node = new PortalNode(SambaConfiguration.builder().build());
        node.start();

    }
}