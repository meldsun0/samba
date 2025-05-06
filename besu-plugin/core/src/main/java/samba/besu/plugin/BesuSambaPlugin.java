package samba.besu.plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


import api.HistoryService;
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
import rpc.SambaVersionRPCMethod;
import samba.Samba;

@AutoService(BesuPlugin.class)
public class BesuSambaPlugin implements BesuPlugin {

    private static final String PLUGIN_NAME = "samba";
    private static final String CLI_OPTIONS_PREFIX = PLUGIN_NAME;

    private static final Logger LOG = LoggerFactory.getLogger(BesuSambaPlugin.class);
    private ServiceManager serviceManager;
    private Optional<Samba> samba = Optional.empty();


    @CommandLine.Option(names = "--plugin-samba-host")
    public String host = "0.0.0.0";


    private static final Map<String, Object> SAMBA_PLUGIN_CONFIG_MAP = new HashMap();

    private BesuConfiguration besuConfigurationService;
    private MetricCategoryRegistry metricCategoryRegistryService;
    private RpcEndpointService rpcEndpointService;
    private PicoCLIOptions picoCLIOptionsService;
    protected MetricsSystem metricsSystemService;

    private static final AtomicBoolean registrationTaskDone = new AtomicBoolean(false);
    private static final AtomicBoolean startingTasksDone = new AtomicBoolean(false);

    @Override
    public void register(ServiceManager serviceManager) {
        LOG.debug("Registering Samba plugin");
        this.serviceManager = serviceManager;
        if (registrationTaskDone.compareAndSet(false, true)) {
            this.besuConfigurationService = this.getBesuService(this.serviceManager, BesuConfiguration.class);
            this.metricCategoryRegistryService = this.getBesuService(this.serviceManager, MetricCategoryRegistry.class);
            this.picoCLIOptionsService = this.getBesuService(this.serviceManager, PicoCLIOptions.class);
            this.rpcEndpointService = this.getBesuService(this.serviceManager, RpcEndpointService.class);
        }
    }


    private void starRpcEndpoints() {
        var methods = List.of(new SambaVersionRPCMethod());
        methods.forEach(
                method -> {
                    LOG.info("Registering RPC plugin endpoint {}_{}", method.getNamespace(), method.getName());
                    rpcEndpointService.registerRPCEndpoint(method.getNamespace(), method.getName(), method::execute);
                });
    }

    private void startPicoCliOptions() {
        this.picoCLIOptionsService.addPicoCLIOptions(PLUGIN_NAME, this);
    }


    @Override
    public Optional<String> getName() {
        return Optional.of(PLUGIN_NAME);
    }

    @Override
    public void start() {
        LOG.info("Starting Samba plugin");
        if (startingTasksDone.compareAndSet(false, true)) {
            this.startPicoCliOptions();
            //TODO check if should be here or not!
            this.metricsSystemService = this.getBesuService(this.serviceManager, MetricsSystem.class);
            this.initSamba();
            this.starRpcEndpoints();
        }
    }

    private void initSamba() {
        try {
            LOG.info("Starting Samba {}", this.getClass().getName());
            String[] options = {"--portal-subnetworks=history-network", "--p2p-advertised-ip=" + host};
            Samba.init(options);

        } catch (Exception e) {
            LOG.error("Halting Besu startup: exception in plugin startup: ", e);
            e.printStackTrace();
            System.exit(1);
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
        //TODO should we do something with Samba  | its db ?
    }


    private <T extends BesuService> T getBesuService(ServiceManager context, Class<T> clazz) {
        return context.getService(clazz).orElseThrow(() -> new RuntimeException("Unable to find given Besu service. Please ensure %s is registered.".formatted(clazz.getName())));
    }

    public static HistoryService getHistoryAPI(){

        return  null;
    }
}
