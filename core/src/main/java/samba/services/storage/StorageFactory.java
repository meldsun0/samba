package samba.services.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.StorageConfig;
import samba.rocksdb.RocksDbInstanceFactory;
import samba.rocksdb.exceptions.DatabaseStorageException;
import samba.rocksdb.keyvalue.KvStoreAccessor;
import samba.rocksdb.keyvalue.KvStoreConfiguration;
import samba.rocksdb.metadata.V6DatabaseMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static samba.metrics.SambaMetricCategory.STORAGE;

public class StorageFactory {

    private static final Logger LOG = LogManager.getLogger();

    static final String DB_PATH = "db";
    static final String DB_VERSION_PATH = "db.version"; // TODO have version file
    static final String METADATA_FILENAME = "metadata.yml";

    private final File dataDirectory;
    private final File dbDirectory;

    private final MetricsSystem metricsSystem;

    public StorageFactory(final MetricsSystem metricsSystem, final Path dataPath) {
        this.metricsSystem = metricsSystem;
        this.dataDirectory = dataPath.toFile();
        this.dbDirectory = this.dataDirectory.toPath().resolve(DB_PATH).toFile();

    }


    public void create2(final MetricsSystem metricsSystem, StorageConfig storageConfig)  throws IOException {
        LOG.info("Beacon data directory set to: {}", dataDirectory.getAbsolutePath());
        validateDataPaths();
        createDirectories();
        //TODO do we need versions ?


        final V6DatabaseMetadata metaData = V6DatabaseMetadata.init(getMetadataFile(), V6DatabaseMetadata.singleDBDefault());
        final KvStoreConfiguration dbConfiguration = metaData.getSingleDbConfiguration().getConfiguration();

        final V6SchemaCombinedSnapshot schema = V6SchemaCombinedSnapshot.createV6(spec);

        final KvStoreAccessor db =
                RocksDbInstanceFactory.create(
                        metricsSystem,
                        STORAGE,
                        dbConfiguration.withDatabaseDir(dbDirectory.toPath()), //KvStoreConfiguration
                        schema.getAllColumns(),
                        schema.getDeletedColumnIds());

        return KvStoreDatabase.createWithStateSnapshots(

                db, schema, stateStorageMode, stateStorageFrequency, storeNonCanonicalBlocks, spec);




        return null;
    }

    private KvStoreConfiguration initConfiguration() throws IOException {




        return
    }

    private File getMetadataFile() {
        return dataDirectory.toPath().resolve(METADATA_FILENAME).toFile();
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
