package samba.services.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.StorageConfig;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;

import tech.pegasys.teku.service.serviceutils.Service;

import java.nio.file.Paths;


public class StorageService extends Service {

    private final StorageConfig storageConfig;
    private final AsyncRunner asyncRunner;
    private final MetricsSystem metricsSystem;

    private volatile HistoryRocksDB database;
    private static final Logger LOG = LogManager.getLogger();


    public StorageService(final MetricsSystem metricsSystem, final AsyncRunner asyncRunner, final StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.asyncRunner =  asyncRunner;
        this.metricsSystem = metricsSystem;
    }

    @Override
    protected SafeFuture<?> doStart() {
        return SafeFuture.fromRunnable(
                        () -> {
                            StorageFactory storageFactory = new StorageFactory(this.metricsSystem,  Paths.get(""));
                            database = storageFactory.create();
                        });
    }


    @Override
    protected SafeFuture<?> doStop() {
        return SafeFuture.fromRunnable(database::close);
    }

    public HistoryRocksDB getDatabase() { return this.database; }

}

