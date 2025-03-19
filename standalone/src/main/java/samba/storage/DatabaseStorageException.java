package samba.storage;

public class DatabaseStorageException extends RuntimeException {
  private final boolean unrecoverable;

  private DatabaseStorageException(
      final String message, final boolean unrecoverable, final Throwable cause) {
    super(message, cause);
    this.unrecoverable = unrecoverable;
  }

  public static DatabaseStorageException recoverable(final String message, final Throwable cause) {
    return new DatabaseStorageException(message, false, cause);
  }

  public static DatabaseStorageException unrecoverable(
      final String message, final Throwable cause) {
    return new DatabaseStorageException(message, true, cause);
  }

  public static DatabaseStorageException unrecoverable(final String message) {
    return new DatabaseStorageException(message, true, null);
  }

  public boolean isUnrecoverable() {
    return unrecoverable;
  }
}
