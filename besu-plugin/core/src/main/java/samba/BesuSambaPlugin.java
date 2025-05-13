package samba;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.auto.service.AutoService;
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
import samba.Samba;
import samba.SambaSDK;
import samba.rpc.GetBlockBodyByBlockHash;

@AutoService(BesuPlugin.class)
public class BesuSambaPlugin implements BesuPlugin {
  private static final Logger LOG = LoggerFactory.getLogger(BesuSambaPlugin.class);
  public static final String PLUGIN_NAME = "samba";
  private static final String CLI_OPTIONS_PREFIX = "--plugin-" + PLUGIN_NAME+"-";

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

  private SambaSDK sambaSDK;

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
      this.metricsSystemService = this.getBesuService(this.serviceManager, MetricsSystem.class);
      this.sambaSDK = this.initSamba();
      starRpcEndpoints();
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
        "--disable-rest--server"
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
    var methods = List.of(new GetBlockBodyByBlockHash(this.sambaSDK));
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
}

/*
  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockHash(Hash blockHash) {
    return this.sambaSDK
        .historyAPI()
        .flatMap(history -> history.getBlockHeaderByBlockHash(blockHash));
  }

  @Override
  public Optional<BlockBody> getBlockBodyByBlockHash(Hash blockHash) {
    return this.sambaSDK
        .historyAPI()
        .flatMap(history -> history.getBlockBodyByBlockHash(blockHash));
  }

  @Override
  public Optional<List<TransactionReceipt>> getTransactionReceiptByBlockHash(Hash blockHash) {
    return this.sambaSDK.historyAPI().flatMap(history -> history.getReceiptByBlockHash(blockHash));
  }

  @Override
  public Optional<BlockHeader> getBlockHeaderByBlockNumber(long blockNumber) {
    return this.sambaSDK
        .historyAPI()
        .flatMap(history -> history.getBlockHeaderByBlockNumber(blockNumber));
  }
}
*/
