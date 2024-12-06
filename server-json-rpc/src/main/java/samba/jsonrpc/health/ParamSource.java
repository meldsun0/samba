package samba.jsonrpc.health;

@FunctionalInterface
public interface ParamSource {
  String getParam(String name);
}
