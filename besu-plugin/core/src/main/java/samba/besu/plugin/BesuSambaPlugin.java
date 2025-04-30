package samba.besu.plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.hyperledger.besu.plugin.services.BesuConfiguration;
import org.hyperledger.besu.plugin.services.BlockchainService;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.PicoCLIOptions;
import org.hyperledger.besu.plugin.services.RpcEndpointService;
import org.hyperledger.besu.plugin.services.metrics.MetricCategoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import rpc.SambaVersionRPCMethod;
import samba.Samba;

@AutoService(BesuPlugin.class)
public class BesuSambaPlugin implements BesuPlugin {

    private static final String PLUGIN_NAME = "samba";
    private static final Logger LOG = LoggerFactory.getLogger(BesuSambaPlugin.class);
    private ServiceManager serviceManager;
    protected BesuConfiguration besuConfiguration;
    protected BlockchainService blockchainService;

    @CommandLine.Option(names = "--plugin-samba-host")
    public String host="0.0.0.0";


    @Override
    public void register(ServiceManager serviceManager) {
        LOG.debug("Registering Samba plugin");
        this.serviceManager = serviceManager;
        this.serviceManager
                .getService(MetricCategoryRegistry.class)
                .ifPresentOrElse(
                        this::registerMetricCategory,
                        () -> LOG.error("No MetricCategoryRegistry service found"));
        this.serviceManager
                .getService(PicoCLIOptions.class)
                .ifPresentOrElse(
                        this::registerPicoCliOptions, () -> LOG.error("No PicoCLIOptions service found"));
        this.serviceManager
                .getService(RpcEndpointService.class)
                .ifPresentOrElse(
                        this::registerRpcEndpoints, () -> LOG.error("No RpcEndpoint service found"));

        this.besuConfiguration =
                serviceManager
                        .getService(BesuConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Failed to obtain BesuConfiguration from the ServiceManager."));
    }

    private void registerRpcEndpoints(RpcEndpointService rpcEndpointService) {
        SambaVersionRPCMethod sambaVersionRPCMethod = new SambaVersionRPCMethod();
        rpcEndpointService.registerRPCEndpoint(
                sambaVersionRPCMethod.getNamespace(),
                sambaVersionRPCMethod.getName(),
                sambaVersionRPCMethod::execute);
    }

    private void registerPicoCliOptions(PicoCLIOptions picoCLIOptions) {
        picoCLIOptions.addPicoCLIOptions(PLUGIN_NAME, this );
    }

    private void registerMetricCategory(MetricCategoryRegistry metricCategoryRegistry) {
    }

    @Override
    public Optional<String> getName() {
        return Optional.of(PLUGIN_NAME);
    }

    @Override
    public void start() {
        LOG.info("Starting Samba plugin");
        this.serviceManager
                .getService(MetricsSystem.class)
                .ifPresentOrElse(this::startMetrics, () -> LOG.error("Could not obtain MetricsSystem"));
    String[] options = {"--portal-subnetworks=history-network", "--p2p-advertised-ip="+host};
    Samba.init(options);
    }

    private void startMetrics(MetricsSystem metricsSystem) {
    }

    @Override
    public void stop() {
        LOG.info("Stopping Samba plugin");
    }
}
