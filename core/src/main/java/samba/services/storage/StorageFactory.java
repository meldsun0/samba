package samba.services.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.storage.HistoryRocksDB;
import samba.storage.rocksdb.RocksDBMetricsFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class StorageFactory {

    private static final Logger LOG = LogManager.getLogger();

    static final String DB_PATH = "db";
    static final String DB_VERSION_PATH = "db.version"; // TODO have version file

    private final File dataDirectory;
    private final File dbDirectory;

    private final MetricsSystem metricsSystem;

    public StorageFactory(final MetricsSystem metricsSystem, final Path dataDirectory) {
        this.metricsSystem = metricsSystem;
        this.dataDirectory = dataDirectory.toFile();
        this.dbDirectory = this.dataDirectory.toPath().resolve(DB_PATH).toFile();

    }

    public HistoryRocksDB create() throws IOException {
        LOG.info("History data directory set to: {}", dataDirectory.getAbsolutePath());
        validateDataPaths();
        createDirectories();
        //TODO do we need versions, and a metadata file ?
        return new HistoryRocksDB(this.dataDirectory.toPath(), this.metricsSystem, RocksDBMetricsFactory.PUBLIC_ROCKS_DB_METRICS);

    }

    private void validateDataPaths() {
        if (dbDirectory.exists()) {
            throw DatabaseStorageException.unrecoverable(String.format("The database path %s exists.", dataDirectory.getAbsolutePath()));
        }
    }

    private void createDirectories() {
        if (!dbDirectory.mkdirs() && !dbDirectory.isDirectory()) {
            throw DatabaseStorageException.unrecoverable(String.format("Unable to create the path to store database files at %s", dbDirectory.getAbsolutePath()));
        }
    }
}
