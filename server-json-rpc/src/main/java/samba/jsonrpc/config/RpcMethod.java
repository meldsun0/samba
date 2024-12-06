package samba.jsonrpc.config;

import java.util.Collection;
import java.util.HashSet;

public enum RpcMethod {
  CLIENT_VERSION("clientVersion"),

  DISCV5_NODE_INFO("discv5_nodeInfo"), // DONE
  DISCV5_UPDATE_NODE_INFO("discv5_updateNodeInfo"), // DONE

  DISCV5_ROUTING_TABLE_INFO("discv5_routingTableInfo"), // TODO need to change disvc5 library

  DISCV5_ADD_ENR("discv5_addEnr"), // TODO need to change disvc5 library
  DISCV5_GET_ENR("discv5_getEnr"), // DONE
  DISCV5_DELETE_ENR("discv5_deleteEnr"), // TODO need to change disvc5 library
  DISCV5_LOOK_UP_ENR("discv5_lookupEnr"),

  DISCV5_PING("discv5_ping"),
  DISCV5_FIND_NODE("discv5_findNode"),
  DISCV5_TALK_REQ("discv5_talkReq"),
  DISCV5_RECURSIVE_FIND_NODES("discv5_recursiveFindNodes"),

  PORTAL_HISTORY_ROUTING_TABLE_INFO("portal_historyRoutingTableInfo"),

  PORTAL_HISTORY_ADD_ENR("portal_historyAddEnr"),
  PORTAL_HISTORY_GET_ENR("portal_historyGetEnr"),
  PORTAL_HISTORY_DELETE_ENR("portal_historyDeleteEnr"),
  PORTAL_HISTORY_LOOKUP_ENR("portal_historyLookupEnr"),

  PORTAL_HISTORY_PING("portal_historyPing"),
  PORTAL_HISTORY_FIND_NODES("portal_historyFindNodes"),
  PORTAL_HISTORY_FIND_CONTENT("portal_historyFindContent"),
  PORTAL_HISTORY_OFFER("portal_historyOffer"),

  PORTAL_HISTORY_RECURSIVE_FIND_NODES("portal_historyRecursiveFindNodes"),
  PORTAL_HISTORY_GOSSIP("portal_historyGossip"),

  PORTAL_HISTORY_GET_CONTENT("portal_historyGetContent"),
  PORTAL_HISTORYT_RACE_GET_CONTENT("portal_historyTraceGetContent"),

  PORTAL_HISTORY_STORE("portal_historyStore"),
  PORTAL_HISTORY_LOCAL_CONTENT("portal_historyLocalContent");

  private final String methodName;

  private static final Collection<String> allMethodNames;

  public String getMethodName() {
    return methodName;
  }

  static {
    allMethodNames = new HashSet<>();
    for (RpcMethod m : RpcMethod.values()) {
      allMethodNames.add(m.getMethodName());
    }
  }

  RpcMethod(final String methodName) {
    this.methodName = methodName;
  }

  public static boolean rpcMethodExists(final String rpcMethodName) {
    return allMethodNames.contains(rpcMethodName);
  }
}
