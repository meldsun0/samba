package samba.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.util.Functions;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.DiscoveryConfig;
import samba.schema.DefaultScheme;
import samba.services.discovery.DiscV5Service;
import samba.services.discovery.DiscoveryService;
import samba.services.discovery.SchemaDefinitionsSupplier;
import samba.store.KeyValueStore;
import samba.store.MemKeyValueStore;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.service.serviceutils.Service;
import tech.pegasys.teku.service.serviceutils.ServiceConfig;

import java.util.Optional;
import java.util.Random;

import static com.google.common.base.Preconditions.checkNotNull;
import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;

//Check DiscoveryNetwork
public class PortalNodeService extends Service {

    private static final Logger LOG = LogManager.getLogger();
    protected static final String KEY_VALUE_STORE_SUBDIRECTORY = "kvstore";
    private static final int DEFAULT_ASYNC_P2P_MAX_THREADS = 10;
    public static final int DEFAULT_ASYNC_P2P_MAX_QUEUE = DEFAULT_MAX_QUEUE_SIZE;


    protected volatile KeyValueStore<String, Bytes> keyValueStore;
    protected volatile EventChannels eventChannels;
    protected volatile MetricsSystem metricsSystem;
    protected volatile TimeProvider timeProvider;
    protected volatile AsyncRunner networkAsyncRunner;
    private final Bytes privKey;


    private DiscoveryService discoveryService;

    public PortalNodeService(final ServiceConfig serviceConfig) {
        this.timeProvider = serviceConfig.getTimeProvider();
        this.eventChannels = serviceConfig.getEventChannels();
        this.metricsSystem = serviceConfig.getMetricsSystem();
        this.networkAsyncRunner = serviceConfig.createAsyncRunner("p2p", DEFAULT_ASYNC_P2P_MAX_THREADS, DEFAULT_ASYNC_P2P_MAX_QUEUE);
        keyValueStore = new MemKeyValueStore<>();
        privKey = Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey().bytes(); //:S
        createDiscoveryService();

    }


    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());

        return SafeFuture.allOfFailFast(discoveryService.start());
             //   .thenCompose(__ -> connectionManager.start())
              //  .thenRun(() -> getEnr().ifPresent(StatusLogger.STATUS_LOG::listeningForDiscv5));

//        return initialize()
//                .thenCompose(
//                        (__) ->
//                                beaconRestAPI.map(BeaconRestApi::start).orElse(SafeFuture.completedFuture(null)));
    }

    @Override
    protected SafeFuture<?> doStop() {
        return null;
    }

//    protected SafeFuture<?> initialize() {
//        return  SafeFuture.allOf(createDiscoveryService());
//
//
//    }


    protected void createDiscoveryService() {
        LOG.info("PortalNodeService.createDiscoveryService()");

        this.discoveryService = new DiscV5Service(
                metricsSystem,
                networkAsyncRunner,
                DiscoveryConfig.builder().build(),
                keyValueStore,
                privKey,
                DiscV5Service.createDefaultDiscoverySystemBuilder(),
                DiscV5Service.DEFAULT_NODE_RECORD_CONVERTER);

    }


}
