package samba.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.plugin.services.MetricsSystem;

import samba.config.DiscoveryConfig;
import samba.config.SambaConfiguration;
import samba.domain.messages.IncomingRequestHandler;
import samba.domain.messages.MessageType;
import samba.domain.messages.handler.FindContentHandler;
import samba.domain.messages.handler.FindNodesHandler;
import samba.domain.messages.handler.OfferHandler;
import samba.domain.messages.handler.PingHandler;
import samba.jsonrpc.config.JsonRpcConfiguration;
import samba.jsonrpc.config.RpcMethod;
import samba.jsonrpc.health.HealthService;
import samba.jsonrpc.health.LivenessCheck;
import samba.jsonrpc.reponse.JsonRpcMethod;
import samba.network.NetworkType;
import samba.network.history.HistoryNetwork;
import samba.services.api.PortalAPI;
import samba.services.api.PortalRestAPI;
import samba.services.connecton.ConnectionService;
import samba.services.discovery.Bootnodes;
import samba.services.discovery.Discv5Service;
import samba.services.jsonrpc.JsonRpcService;
import samba.services.jsonrpc.methods.ClientVersion;
import samba.services.jsonrpc.methods.discv5.Discv5GetEnr;
import samba.services.jsonrpc.methods.discv5.Discv5NodeInfo;
import samba.services.jsonrpc.methods.discv5.Discv5RoutingTableInfo;
import samba.services.jsonrpc.methods.discv5.Discv5UpdateNodeInfo;
import samba.services.storage.StorageService;
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
    protected volatile Optional<JsonRpcService> jsonRpcService = Optional.empty();

    private final Vertx vertx;
    private Discv5Service discoveryService;
    private ConnectionService connectionService;
    private HistoryNetwork historyNetwork;
    private StorageService storageService;
    private final IncomingRequestHandler incomingRequestProcessor = new IncomingRequestHandler();


    public PortalNodeMainService(final MainServiceConfig mainServiceConfig, final SambaConfiguration sambaConfiguration, final Vertx vertx) {
        this.timeProvider = mainServiceConfig.getTimeProvider();
        this.eventChannels = mainServiceConfig.getEventChannels();
        this.metricsSystem = mainServiceConfig.getMetricsSystem();
        this.asyncRunner = mainServiceConfig.createAsyncRunner("p2p", DEFAULT_ASYNC_P2P_MAX_THREADS, DEFAULT_ASYNC_P2P_MAX_QUEUE);
        this.sambaConfiguration = sambaConfiguration;
        this.vertx = vertx;
        initDiscoveryService();
        initStorageService();
        initHistoryNetwork();
        initConnectionService();
        initRestAPI();
        initJsonRPCService();

    }

    private void initJsonRPCService() {
        final JsonRpcConfiguration jsonRpcConfiguration = JsonRpcConfiguration.createDefault();
        if (jsonRpcConfiguration.isEnabled()) {
            final Map<String, JsonRpcMethod> methods = new HashMap<>();

            methods.put(RpcMethod.CLIENT_VERSION.getMethodName(), new ClientVersion("1"));
            methods.put(RpcMethod.DISCV5_NODE_INFO.getMethodName(), new Discv5NodeInfo(this.discoveryService));
            methods.put(RpcMethod.DISCV5_UPDATE_NODE_INFO.getMethodName(), new Discv5UpdateNodeInfo(this.discoveryService));
            methods.put(RpcMethod.DISCV5_UPDATE_NODE_INFO.getMethodName(), new Discv5UpdateNodeInfo(this.discoveryService));
            methods.put(RpcMethod.DISCV5_GET_ENR.getMethodName(), new Discv5GetEnr(this.discoveryService));


            jsonRpcService = Optional.of(new JsonRpcService(this.vertx, jsonRpcConfiguration, metricsSystem, methods, new HealthService(new LivenessCheck())));
        }

    }

    private void initHistoryNetwork() {
        LOG.info("PortalNodeMainService.initHistoryNetwork()");
        //Get and initialize HistoryDB object from persistent storage
        this.historyNetwork = new HistoryNetwork(this.discoveryService, this.storageService.getDatabase());
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

    protected void initStorageService() {
        LOG.info("PortalNodeMainService.initStorageService()");
        this.storageService = new StorageService(this.metricsSystem, this.asyncRunner, null);

    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());
        this.incomingRequestProcessor.build(this.historyNetwork);
        return SafeFuture.allOfFailFast(discoveryService.start())
                .thenCompose(__ -> connectionService.start())
                .thenCompose(__ -> jsonRpcService.map(JsonRpcService::start).orElse(SafeFuture.completedFuture(null)))
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
