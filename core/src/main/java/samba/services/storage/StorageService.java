package samba.services.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import samba.config.DiscoveryConfig;
import samba.config.StorageConfig;
import samba.rocksdb.KvStoreColumn;
import samba.rocksdb.RocksDbInstance;
import samba.rocksdb.RocksDbInstanceFactory;
import samba.rocksdb.exceptions.DatabaseStorageException;
import samba.rocksdb.keyvalue.KvStoreConfiguration;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.async.eventthread.AsyncRunnerEventThread;
import tech.pegasys.teku.infrastructure.events.EventChannels;

import tech.pegasys.teku.service.serviceutils.Service;
import tech.pegasys.teku.service.serviceutils.ServiceConfig;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;


public class StorageService extends Service {

    private final StorageConfig storageConfig;
    private final AsyncRunner asyncRunner;
    private final MetricsSystem metricsSystem;

    private volatile Database database;
    private static final Logger LOG = LogManager.getLogger();


    public StorageService(
            final MetricsSystem metricsSystem,
            final AsyncRunner asyncRunner,
            final StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
        this.asyncRunner =  asyncRunner;
        this.metricsSystem = metricsSystem;


    }

    @Override
    protected SafeFuture<?> doStart() {
        return null;
//        return SafeFuture.fromRunnable(
//                        () -> {


//
//
//
//                            StorageFactory storageFactory = new StorageFactory(this.metricsSystem, null);
//
//
//                            final MetricsSystem metricsSystem,
//                            final MetricCategory metricCategory,
//                            final KvStoreConfiguration configuration,
//                            final Collection<KvStoreColumn<?, ?>> columns,
//                            final Collection<Bytes> deletedColumns)
//            throws DatabaseStorageException {
//
//
//
//                            }
//                            RocksDbInstanceFactory.create(this.metricsSystem,)
//                            final AsyncRunner storagePrunerAsyncRunner =
//                                    serviceConfig.createAsyncRunner(
//                                            "storagePrunerAsyncRunner",
//                                            1,
//                                            DEFAULT_MAX_QUEUE_SIZE,
//                                            Thread.NORM_PRIORITY - 1);
//                            final VersionedDatabaseFactory dbFactory =
//                                    new VersionedDatabaseFactory(
//                                            serviceConfig.getMetricsSystem(),
//                                            serviceConfig.getDataDirLayout().getBeaconDataDirectory(),
//                                            config);
//
//                            database = RocksDbInstanceFactory.create2(this.metricsSystem, this.storageConfig);
//
//                            final SettableLabelledGauge pruningTimingsLabelledGauge =
//                                    SettableLabelledGauge.create(
//                                            serviceConfig.getMetricsSystem(),
//                                            TekuMetricCategory.STORAGE,
//                                            "pruning_time",
//                                            "Tracks last pruning duration in milliseconds",
//                                            "type");
//
//                            final SettableLabelledGauge pruningActiveLabelledGauge =
//                                    SettableLabelledGauge.create(
//                                            serviceConfig.getMetricsSystem(),
//                                            TekuMetricCategory.STORAGE,
//                                            "pruning_active",
//                                            "Tracks when pruner is active",
//                                            "type");
//
//                            if (!config.getDataStorageMode().storesAllBlocks()) {
//                                blockPruner =
//                                        Optional.of(
//                                                new BlockPruner(
//                                                        config.getSpec(),
//                                                        database,
//                                                        storagePrunerAsyncRunner,
//                                                        config.getBlockPruningInterval(),
//                                                        config.getBlockPruningLimit(),
//                                                        "block",
//                                                        pruningTimingsLabelledGauge,
//                                                        pruningActiveLabelledGauge));
//                            }
//                            if (config.getDataStorageMode().storesFinalizedStates()
//                                    && config.getRetainedSlots() > 0) {
//                                if (config.getDataStorageCreateDbVersion() == DatabaseVersion.LEVELDB_TREE) {
//                                    throw new InvalidConfigurationException(
//                                            "State pruning is not supported with leveldb_tree database.");
//                                } else {
//                                    LOG.info(
//                                            "State pruner will run every: {} minute(s), retaining states for the last {} finalized slots. Limited to {} state prune per execution. ",
//                                            config.getStatePruningInterval().toMinutes(),
//                                            config.getRetainedSlots(),
//                                            config.getStatePruningLimit());
//                                    statePruner =
//                                            Optional.of(
//                                                    new StatePruner(
//                                                            config.getSpec(),
//                                                            database,
//                                                            storagePrunerAsyncRunner,
//                                                            config.getStatePruningInterval(),
//                                                            config.getRetainedSlots(),
//                                                            config.getStatePruningLimit(),
//                                                            "state",
//                                                            pruningTimingsLabelledGauge,
//                                                            pruningActiveLabelledGauge));
//                                }
//                            }
//                            if (config.getSpec().isMilestoneSupported(SpecMilestone.DENEB)) {
//                                blobsPruner =
//                                        Optional.of(
//                                                new BlobSidecarPruner(
//                                                        config.getSpec(),
//                                                        database,
//                                                        serviceConfig.getMetricsSystem(),
//                                                        storagePrunerAsyncRunner,
//                                                        serviceConfig.getTimeProvider(),
//                                                        config.getBlobsPruningInterval(),
//                                                        config.getBlobsPruningLimit(),
//                                                        blobSidecarsStorageCountersEnabled,
//                                                        "blob_sidecar",
//                                                        pruningTimingsLabelledGauge,
//                                                        pruningActiveLabelledGauge,
//                                                        config.isStoreNonCanonicalBlocksEnabled()));
//                            }
//                            final EventChannels eventChannels = serviceConfig.getEventChannels();
//                            chainStorage =
//                                    ChainStorage.create(
//                                            database,
//                                            config.getSpec(),
//                                            config.getDataStorageMode(),
//                                            config.getStateRebuildTimeoutSeconds());
//                            final DepositStorage depositStorage =
//                                    DepositStorage.create(
//                                            eventChannels.getPublisher(Eth1EventsChannel.class),
//                                            database,
//                                            depositSnapshotStorageEnabled);
//
//                            batchingVoteUpdateChannel =
//                                    new BatchingVoteUpdateChannel(
//                                            chainStorage,
//                                            new AsyncRunnerEventThread(
//                                                    "batch-vote-updater", serviceConfig.getAsyncRunnerFactory()));
//
//                            eventChannels.subscribe(
//                                    CombinedStorageChannel.class,
//                                    new CombinedStorageChannelSplitter(
//                                            serviceConfig.createAsyncRunner(
//                                                    "storage_query", STORAGE_QUERY_CHANNEL_PARALLELISM),
//                                            new RetryingStorageUpdateChannel(
//                                                    chainStorage, serviceConfig.getTimeProvider()),
//                                            chainStorage));
//
//                            eventChannels
//                                    .subscribe(Eth1DepositStorageChannel.class, depositStorage)
//                                    .subscribe(Eth1EventsChannel.class, depositStorage)
//                                    .subscribe(VoteUpdateChannel.class, batchingVoteUpdateChannel);
//                        })
//                .thenCompose(
//                        __ ->
//                                blockPruner
//                                        .map(BlockPruner::start)
//                                        .orElseGet(() -> SafeFuture.completedFuture(null)))
//                .thenCompose(
//                        __ ->
//                                blobsPruner
//                                        .map(BlobSidecarPruner::start)
//                                        .orElseGet(() -> SafeFuture.completedFuture(null)))
//                .thenCompose(
//                        __ ->
//                                statePruner
//                                        .map(StatePruner::start)
//                                        .orElseGet(() -> SafeFuture.completedFuture(null)));
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
//        blockPruner
//                .map(BlockPruner::stop)
//                .orElseGet(() -> SafeFuture.completedFuture(null))
//                .thenCompose(__ -> SafeFuture.fromRunnable(database::close));
    }

}

