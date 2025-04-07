package samba;

import samba.services.PortalNode;

public final class SambaStandalone {

  public static void main(String[] args) {
    PortalNode node = new PortalNode(args);
    node.start();
  }
}
