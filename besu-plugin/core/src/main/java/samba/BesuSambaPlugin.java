package samba;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.ethereum.core.BlockBody;
import org.hyperledger.besu.ethereum.core.BlockHeader;
import org.hyperledger.besu.ethereum.core.TransactionReceipt;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.BesuConfiguration;
import org.hyperledger.besu.plugin.services.BesuService;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.RpcEndpointService;
import org.hyperledger.besu.plugin.services.metrics.MetricCategoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import samba.api.HistoryService;
import samba.rpc.GetBlockBodyByBlockHash;
import samba.rpc.GetBlockHeaderByBlockHash;
import samba.rpc.GetTransactionReceiptByBlockHash;

@AutoService(BesuPlugin.class)
public class BesuSambaPlugin implements BesuPlugin, HistoryService {
  private static final Logger LOG = LoggerFactory.getLogger(BesuSambaPlugin.class);
  public static final String PLUGIN_NAME = "samba";
  private static final String CLI_OPTIONS_PREFIX = "--plugin-" + PLUGIN_NAME + "-";

  private ServiceManager serviceManager;
  private MetricCategoryRegistry metricCategoryRegistryService;
  private BesuConfiguration besuConfigurationService;
  private PicoCLIOptions picoCLIOptionsService;
  protected MetricsSystem metricsSystemService;
  private RpcEndpointService rpcEndpointService;

  private static final AtomicBoolean registrationTaskDone = new AtomicBoolean(false);
  private static final AtomicBoolean startingTasksDone = new AtomicBoolean(false);

  @CommandLine.Option(names = CLI_OPTIONS_PREFIX + "host")
  public String host = "0.0.0.0";

  @CommandLine.Option(names = {"--plugin-samba-logging"})
  public String loggingLevel;

  @CommandLine.Option(names = {"--plugin-samba-data-path"})
  public String dataPath;

  private final CompletableFuture<SambaSDK> sambaSDKFuture = new CompletableFuture<>();

  @Override
  public void register(ServiceManager serviceManager) {
    LOG.debug("Registering Samba plugin");
    this.serviceManager = serviceManager;
    if (registrationTaskDone.compareAndSet(false, true)) {
      this.metricCategoryRegistryService =
          this.getBesuService(this.serviceManager, MetricCategoryRegistry.class);
      this.besuConfigurationService =
          this.getBesuService(this.serviceManager, BesuConfiguration.class);
      this.picoCLIOptionsService = this.getBesuService(this.serviceManager, PicoCLIOptions.class);
      // TODO create a function
      this.picoCLIOptionsService.addPicoCLIOptions(PLUGIN_NAME, this);
      this.rpcEndpointService = this.getBesuService(this.serviceManager, RpcEndpointService.class);
      this.starRpcEndpoints(); // TODO use a completable future till samba is fully initialized.
    }
  }

  @Override
  public Optional<String> getName() {
    return Optional.of(PLUGIN_NAME);
  }

  @Override
  public void start() {
    LOG.info("Starting Samba plugin");
    if (startingTasksDone.compareAndSet(false, true)) {
      SambaSDK sdk = this.initSamba();
      sambaSDKFuture.complete(sdk);
      this.metricsSystemService = this.getBesuService(this.serviceManager, MetricsSystem.class);
    }
  }

  @Override
  public void stop() {
    LOG.info("Stopping Samba plugin");
    registrationTaskDone.set(false);
    startingTasksDone.set(false);
    this.besuConfigurationService = null;
    this.metricCategoryRegistryService = null;
    this.rpcEndpointService = null;
    this.picoCLIOptionsService = null;
    // TODO should we do something with Samba  | its db ?
    // TODO call samba to stop
  }

  private SambaSDK initSamba() {
    try {
      String[] options = {
        "--portal-subnetworks=history-network",
        "--p2p-advertised-ip=" + host,
        "--disable-json-rpc-server",
        "--disable-rest--server",
        "--logging=" + loggingLevel,
        "--data-path=" + dataPath
      };
      return Samba.init(options);

    } catch (Exception e) {
      LOG.error("Halting Besu startup: exception in plugin startup: ", e);
      e.printStackTrace();
      System.exit(1);
      return null; // unreachable, but required to compile
    }
  }

  private void starRpcEndpoints() {
    var methods =
        List.of(
            new GetBlockBodyByBlockHash(this.sambaSDKFuture),
            new GetBlockHeaderByBlockHash(this.sambaSDKFuture),
            new GetTransactionReceiptByBlockHash(this.sambaSDKFuture),
            new GetTransactionReceiptByBlockHash(this.sambaSDKFuture));
    methods.forEach(
        method -> {
          LOG.info(
              "Registering RPC plugin endpoint {}_{}", method.getNamespace(), method.getName());
          rpcEndpointService.registerRPCEndpoint(
              method.getNamespace(), method.getName(), method::execute);
        });
  }

  private <T extends BesuService> T getBesuService(ServiceManager context, Class<T> clazz) {
    return context
        .getService(clazz)
        .orElseThrow(
            () ->
                new RuntimeException(
                    "Unable to find given Besu service. Please ensure %s is registered."
                        .formatted(clazz.getName())));
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash) {
    try {
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getBlockHeaderByBlockHash(blockHash));
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing GetBlockHeaderByBlockHash operation");
    }
    return Optional.empty();
  }

  @Override
  public Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash) {
    try {
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getBlockBodyByBlockHash(blockHash));
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing GetBlockBodyByBlockHash operation");
    }
    return Optional.empty();
  }

  @Override
  public Optional<List<TransactionReceipt>> getTransactionReceiptByBlockHash(Hash blockHash) {
    try {
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getTransactionReceiptByBlockHash(blockHash));
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing GetTransactionReceiptByBlockHash operation");
    }
    return Optional.empty();
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockNumber(String blockNumber) {
    try {
      return this.sambaSDKFuture
          .get()
          .historyAPI()
          .flatMap(history -> history.getBlockHeaderByBlockNumber(blockNumber));
    } catch (InterruptedException | ExecutionException e) {
      LOG.debug("Error when executing  GetBlockHeaderByBlockNumber operation");
    }
    return Optional.empty();
  }
}
