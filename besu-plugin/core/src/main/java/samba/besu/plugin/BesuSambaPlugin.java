package samba.besu.plugin;

import com.google.auto.service.AutoService;
import org.hyperledger.besu.plugin.BesuPlugin;
import org.hyperledger.besu.plugin.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@AutoService(BesuPlugin.class)
public class BesuSambaPlugin implements BesuPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(BesuSambaPlugin.class);


    @Override
    public void register(ServiceManager serviceManager) {

    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}
