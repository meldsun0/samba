package samba.rocksdb2.me.ready.kv;

public class StorageException extends RuntimeException {

    public StorageException(final Throwable cause) {
        super(cause);
    }

    public StorageException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public StorageException(final String message) {
        super(message);
    }
}
