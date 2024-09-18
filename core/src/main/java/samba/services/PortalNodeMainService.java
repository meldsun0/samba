package samba.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.DiscoveryConfig;
import samba.config.SambaConfiguration;
import samba.services.api.PortalRestAPI;
import samba.services.api.PortalAPI;
import samba.services.discovery.DiscV5Service;
import samba.store.KeyValueStore;
import samba.store.MemKeyValueStore;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.service.serviceutils.Service;


import java.util.Optional;


import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;

//Check DiscoveryNetwork
public class PortalNodeMainService extends Service {

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

    protected volatile SambaConfiguration sambaConfiguration;
    private DiscoveryService discoveryService;
    protected volatile Optional<PortalRestAPI> portalRestAPI = Optional.empty();

    public PortalNodeMainService(final MainServiceConfig mainServiceConfig, final SambaConfiguration sambaConfiguration) {
        this.timeProvider = mainServiceConfig.getTimeProvider();
        this.eventChannels = mainServiceConfig.getEventChannels();
        this.metricsSystem = mainServiceConfig.getMetricsSystem();
        this.networkAsyncRunner = mainServiceConfig.createAsyncRunner("p2p", DEFAULT_ASYNC_P2P_MAX_THREADS, DEFAULT_ASYNC_P2P_MAX_QUEUE);
        this.sambaConfiguration = sambaConfiguration;

        keyValueStore = new MemKeyValueStore<>();
        privKey =Bytes.fromHexString("0x008110c0cc11ec947b27f722e2394e0abe6d8596208888a3e50768296556bc7152"); // Functions.randomKeyPair(new Random(new Random().nextInt())).secretKey().bytes(); //:S


        initDiscoveryService();
        initRestAPI();

    }


    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());


        //   .thenCompose(__ -> connectionManager.start())
        //     .thenRun(() -> getEnr().ifPresent(StatusLogger.STATUS_LOG::listeningForDiscv5));

     return SafeFuture.allOfFailFast(discoveryService.start())
              .thenCompose((__) -> portalRestAPI.map(PortalRestAPI::start).orElse(SafeFuture.completedFuture(null)));
    }

    @Override
    protected SafeFuture<?> doStop() {
        LOG.debug("Stopping {}", this.getClass().getSimpleName());
        return SafeFuture.allOf(
                discoveryService.stop(),
                portalRestAPI.map(PortalRestAPI::stop).orElse(SafeFuture.completedFuture(null)));


    }

    protected void initDiscoveryService() {
        LOG.info("PortalNodeService.initDiscoveryService()");
        this.discoveryService = new DiscV5Service(
                metricsSystem,
                networkAsyncRunner,
                DiscoveryConfig.builder().build(),
                keyValueStore,
                privKey,
                DiscV5Service.createDefaultDiscoverySystemBuilder(),
                DiscV5Service.DEFAULT_NODE_RECORD_CONVERTER);
    }

    public void initRestAPI() {
        LOG.debug("PortalNodeMainService.initRestAPI()");
        portalRestAPI =
                Optional.of(
                        new PortalAPI(
                                sambaConfiguration.portalRestApiConfig(),
                                eventChannels,
                                networkAsyncRunner,
                                timeProvider));
    }
}
