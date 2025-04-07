package samba.exceptions;

public class FatalServiceFailureException extends RuntimeException {

  private final String service;

  public FatalServiceFailureException(final String serviceName, final String message) {
    super(message);
    this.service = serviceName;
  }

  public FatalServiceFailureException(final Class<?> service, final Throwable cause) {
    super(cause);
    this.service = service.getSimpleName();
  }

  public String getService() {
    return service;
  }
}
