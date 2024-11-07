package samba.services;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import samba.config.DiscoveryConfig;
import samba.config.SambaConfiguration;
import samba.services.storage.HistoryDBImpl;
import samba.domain.messages.IncomingRequestHandler;
import samba.domain.messages.MessageType;
import samba.domain.messages.handler.FindContentHandler;
import samba.domain.messages.handler.FindNodesHandler;
import samba.domain.messages.handler.OfferHandler;
import samba.domain.messages.handler.PingHandler;
import samba.network.NetworkType;
import samba.network.history.HistoryNetwork;
import samba.services.api.PortalAPI;
import samba.services.api.PortalRestAPI;
import samba.services.connecton.ConnectionService;
import samba.services.discovery.Bootnodes;
import samba.services.discovery.Discv5Service;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.service.serviceutils.Service;

//Check DiscoveryNetwork
public class PortalNodeMainService extends Service {

    private static final Logger LOG = LogManager.getLogger();
    private static final int DEFAULT_ASYNC_P2P_MAX_THREADS = 10;
    public static final int DEFAULT_ASYNC_P2P_MAX_QUEUE = DEFAULT_MAX_QUEUE_SIZE;


    protected volatile EventChannels eventChannels;
    protected volatile MetricsSystem metricsSystem;
    protected volatile TimeProvider timeProvider;
    protected volatile AsyncRunner asyncRunner;
    private final Bytes privKey = null;

    protected volatile SambaConfiguration sambaConfiguration;


    protected volatile Optional<PortalRestAPI> portalRestAPI = Optional.empty();

    private Discv5Service discoveryService;
    private ConnectionService connectionService;
    private HistoryNetwork historyNetwork;
    private final IncomingRequestHandler incomingRequestProcessor = new IncomingRequestHandler();


    public PortalNodeMainService(final MainServiceConfig mainServiceConfig, final SambaConfiguration sambaConfiguration) {
        this.timeProvider = mainServiceConfig.getTimeProvider();
        this.eventChannels = mainServiceConfig.getEventChannels();
        this.metricsSystem = mainServiceConfig.getMetricsSystem();
        this.asyncRunner = mainServiceConfig.createAsyncRunner("p2p", DEFAULT_ASYNC_P2P_MAX_THREADS, DEFAULT_ASYNC_P2P_MAX_QUEUE);
        this.sambaConfiguration = sambaConfiguration;

        initDiscoveryService();
        initHistoryNetwork();
        initConnectionService();
        initRestAPI();

    }

    private void initHistoryNetwork() {
        LOG.info("PortalNodeMainService.initHistoryNetwork()");
        //Get and initialize HistoryDB object from persistent storage
        this.historyNetwork = new HistoryNetwork(this.discoveryService, new HistoryDBImpl());
        incomingRequestProcessor
                .addHandler(MessageType.PING, new PingHandler())
                .addHandler(MessageType.FIND_NODES, new FindNodesHandler())
                .addHandler(MessageType.FIND_CONTENT, new FindContentHandler())
                .addHandler(MessageType.OFFER, new OfferHandler());
    }

    private void initConnectionService() {
        LOG.info("PortalNodeMainService.initConnectionService()");
        this.connectionService = new ConnectionService(
                this.metricsSystem,
                this.asyncRunner,
                this.discoveryService,
                this.historyNetwork);

    }

    protected void initDiscoveryService() {
        LOG.info("PortalNodeMainService.initDiscoveryService()");
        this.discoveryService = new Discv5Service(
                this.metricsSystem,
                this.asyncRunner,
                DiscoveryConfig.builder().bootnodes(Bootnodes.loadBootnodes(NetworkType.EXECUTION_HISTORY_NETWORK)).build(),
                this.privKey,
                incomingRequestProcessor);

    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());
        this.incomingRequestProcessor.build(this.historyNetwork);
        return SafeFuture.allOfFailFast(discoveryService.start())
                .thenCompose(__ -> connectionService.start())
                .thenCompose(__ -> portalRestAPI.map(PortalRestAPI::start).orElse(SafeFuture.completedFuture(null)));
    }

    @Override
    protected SafeFuture<?> doStop() {
        LOG.debug("Stopping {}", this.getClass().getSimpleName());
        return SafeFuture.allOf(
                discoveryService.stop(),
                connectionService.stop(),
                portalRestAPI.map(PortalRestAPI::stop).orElse(SafeFuture.completedFuture(null)));


    }

    public void initRestAPI() {
        LOG.debug("PortalNodeMainService.initRestAPI()");
        portalRestAPI =
                Optional.of(
                        new PortalAPI(
                                sambaConfiguration.portalRestApiConfig(),
                                eventChannels,
                                asyncRunner,
                                timeProvider));
    }
}
