package samba.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.metrics.SambaMetricCategory;
import samba.rocksdb.RocksDbInstanceFactory;
import samba.rocksdb.keyvalue.KvStoreAccessor;
import samba.rocksdb.keyvalue.KvStoreConfiguration;
import samba.services.storage.Database;
import samba.rocksdb.exceptions.DatabaseStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class StorageConfig {

    static final String DB_PATH = "db";
    static final String DB_VERSION_PATH = "db.version"; // TODO have version file

    private static final Logger LOG = LogManager.getLogger();
    private final File dataDirectory;
    private final File dbDirectory;

    private final MetricsSystem metricsSystem;

    public StorageConfig(final MetricsSystem metricsSystem, final Path dataPath) {
        this.metricsSystem = metricsSystem;
        this.dataDirectory = dataPath.toFile();
        this.dbDirectory = this.dataDirectory.toPath().resolve(DB_PATH).toFile();

    }


    public static Database createV6(
            final MetricsSystem metricsSystem,
            final KvStoreConfiguration hotConfiguration,
            final SchemaCombinedSnapshotState schema,
            final StateStorageMode stateStorageMode,
            final long stateStorageFrequency,
            final boolean storeNonCanonicalBlocks,
            final Spec spec) {

        final KvStoreAccessor db =
                RocksDbInstanceFactory.create(
                        metricsSystem,
                        STORAGE,
                        hotConfiguration,
                        schema.getAllColumns(),
                        schema.getDeletedColumnIds());

        return KvStoreDatabase.createWithStateSnapshots(
                db, schema, stateStorageMode, stateStorageFrequency, storeNonCanonicalBlocks, spec);
    }


    public Database createDatabase() {
        LOG.info("Portal History data directory set to: {}", dataDirectory.getAbsolutePath());
        validateDataPaths();
        createDirectories();
        //createV6Database() {
        try {

                final KvStoreConfiguration dbConfiguration = KvStoreConfiguration.getDefault();

//                final V6SchemaCombinedSnapshot schema = V6SchemaCombinedSnapshot.createV6(spec);
//                return RocksDbDatabaseFactory.createV6(
//                        metricsSystem,
//                        dbConfiguration.withDatabaseDir(dbDirectory.toPath()),
//                        schema,
//                        stateStorageMode,
//                        stateStorageFrequency,
//                        storeNonCanonicalBlocks,
//                        spec);

            final KvStoreAccessor db =
                    RocksDbInstanceFactory.create(
                            metricsSystem,
                            SambaMetricCategory.STORAGE,
                            dbConfiguration,
                            schema.getAllColumns(),
                            schema.getDeletedColumnIds());

            return KvStoreDatabase.createWithStateSnapshots(
                    db, schema, stateStorageMode, stateStorageFrequency, storeNonCanonicalBlocks, spec);






            } catch (final IOException e) {
                throw DatabaseStorageException.unrecoverable("Failed to read metadata", e);
            }
        }




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
