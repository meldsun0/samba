package samba.network.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException(final String message, final Throwable cause) {
    super(message, cause);
  }

  public BadRequestException(final String message) {
    super(message);
  }
}
