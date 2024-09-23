package samba.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import samba.config.DiscoveryConfig;
import samba.config.SambaConfiguration;
import samba.network.history.HistoryNetwork;
import samba.services.api.PortalRestAPI;
import samba.services.api.PortalAPI;
import samba.services.connecton.ConnectionService;
import samba.services.discovery.Discv5Service;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;
import tech.pegasys.teku.service.serviceutils.Service;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory.DEFAULT_MAX_QUEUE_SIZE;

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
        this.historyNetwork = new HistoryNetwork(this.discoveryService);
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
        List bootnodes = new ArrayList<>();

        //# Trin bootstrap nodes
        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64("-Jy4QIs2pCyiKna9YWnAF0zgf7bT0GzlAGoF8MEKFJOExmtofBIqzm71zDvmzRiiLkxaEJcs_Amr7XIhLI74k1rtlXICY5Z0IDAuMS4xLWFscGhhLjEtMTEwZjUwgmlkgnY0gmlwhKEjVaWJc2VjcDI1NmsxoQLSC_nhF1iRwsCw0n3J4jRjqoaRxtKgsEe5a-Dz7y0JloN1ZHCCIyg"));
        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64("-Jy4QH4_H4cW--ejWDl_W7ngXw2m31MM2GT8_1ZgECnfWxMzZTiZKvHDgkmwUS_l2aqHHU54Q7hcFSPz6VGzkUjOqkcCY5Z0IDAuMS4xLWFscGhhLjEtMTEwZjUwgmlkgnY0gmlwhJ31OTWJc2VjcDI1NmsxoQPC0eRkjRajDiETr_DRa5N5VJRm-ttCWDoO1QAMMCg5pIN1ZHCCIyg"));


        this.discoveryService = new Discv5Service(
                this.metricsSystem,
                this.asyncRunner,
                DiscoveryConfig.builder().build(),
                this.privKey,
                bootnodes);

    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());
        return SafeFuture.allOfFailFast(discoveryService.start())
                .thenCompose(__-> connectionService.start())
                .thenCompose((__) -> portalRestAPI.map(PortalRestAPI::start).orElse(SafeFuture.completedFuture(null)));
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
