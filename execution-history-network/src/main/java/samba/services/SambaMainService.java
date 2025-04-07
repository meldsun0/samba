package samba.services;

import samba.config.MainServiceConfig;
import samba.config.SambaConfig;
import samba.domain.messages.IncomingRequestTalkHandler;
import samba.domain.messages.MessageType;
import samba.domain.messages.handler.FindContentHandler;
import samba.domain.messages.handler.FindNodesHandler;
import samba.domain.messages.handler.HistoryNetworkIncomingRequestHandler;
import samba.domain.messages.handler.OfferHandler;
import samba.domain.messages.handler.PingHandler;
import samba.domain.messages.utp.UTPNetworkIncomingRequestHandler;
import samba.network.history.HistoryNetwork;
import samba.services.connecton.ConnectionService;
import samba.services.discovery.Discv5Service;
import samba.services.storage.HistoryRocksDB;
import samba.services.utp.UTPManager;

import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;

public class SambaMainService extends Service {

  private static final Logger LOG = LoggerFactory.getLogger(SambaMainService.class);

  protected volatile SambaConfig sambaConfig;
  protected volatile AsyncRunner asyncRunner;

  private Discv5Service discoveryService;
  private ConnectionService connectionService;
  private HistoryNetwork historyNetwork;
  private UTPManager utpManager;

  private final IncomingRequestTalkHandler incomingRequestTalkHandler =
      new IncomingRequestTalkHandler();

  public SambaMainService(
      final SambaConfig sambaConfig, final MainServiceConfig mainServiceConfig) {
    this.sambaConfig = sambaConfig;
    this.asyncRunner = mainServiceConfig.createAsyncRunner("p2p");
    initDiscoveryService();
    initUTPService();
    initHistoryNetwork();
    initIncomingRequestTalkHandlers();
    initConnectionService();
  }

  private void initIncomingRequestTalkHandlers() {

    final HistoryNetworkIncomingRequestHandler historyNetworkIncomingRequestHandler =
        new HistoryNetworkIncomingRequestHandler(this.historyNetwork);
    historyNetworkIncomingRequestHandler
        .addHandler(MessageType.PING, new PingHandler())
        .addHandler(MessageType.FIND_NODES, new FindNodesHandler())
        .addHandler(MessageType.FIND_CONTENT, new FindContentHandler())
        .addHandler(MessageType.OFFER, new OfferHandler());

    final UTPNetworkIncomingRequestHandler utpNetworkIncomingRequestHandler =
        new UTPNetworkIncomingRequestHandler(this.utpManager);
    this.incomingRequestTalkHandler.addHandlers(
        historyNetworkIncomingRequestHandler, utpNetworkIncomingRequestHandler);
  }

  private void initUTPService() {
    this.utpManager = new UTPManager(this.discoveryService);
  }

  private void initHistoryNetwork() {
    this.historyNetwork =
        new HistoryNetwork(
            this.discoveryService,
            HistoryRocksDB.create(
                new NoOpMetricsSystem(), this.sambaConfig.getStorageConfig().getDataPath()),
            this.utpManager);
  }

  private void initConnectionService() {
    this.connectionService =
        new ConnectionService(this.asyncRunner, this.discoveryService, this.historyNetwork);
  }

  protected void initDiscoveryService() {
    this.discoveryService =
        new Discv5Service(
            this.sambaConfig.getDiscoveryConfig(),
            this.sambaConfig.getSecreteKey(),
            this.incomingRequestTalkHandler);
  }

  @Override
  protected SafeFuture<?> doStart() {
    LOG.debug("Starting {}", this.getClass().getSimpleName());
    this.incomingRequestTalkHandler.start();
    return SafeFuture.allOfFailFast(this.discoveryService.start())
        .thenCompose(__ -> this.connectionService.start());
  }

  @Override
  protected SafeFuture<?> doStop() {
    LOG.debug("Stopping {}", this.getClass().getSimpleName());
    // TODO STOP DB
    return SafeFuture.allOf(discoveryService.stop(), connectionService.stop());
  }
}
