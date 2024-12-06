package samba.jsonrpc.health;

@FunctionalInterface
public interface HealthCheck {
  boolean isHealthy(ParamSource paramSource);
}
