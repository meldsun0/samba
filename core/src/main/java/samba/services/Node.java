package samba.services;

public interface Node extends AutoCloseable {

  void start();

  void stop();

  @Override
  default void close() {
    stop();
  }
}
