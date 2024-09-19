package samba.services.discovery;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Bootnodes {
    private static final Logger logger = LoggerFactory.getLogger(Bootnodes.class);
    private final List<NodeRecord> bootnodes;
    private final String networkName;

    public Bootnodes(final String networkName) {
        this.networkName = networkName;
        bootnodes = new ArrayList<>();
        loadBootnodes();
    }

    private void loadBootnodes() {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("bootnodes.json");
    

        if (inputStream == null) {
            logger.error("File bootnodes.json not found");
            return;
        }

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode bootnodesNode = rootNode.get(networkName);
            if (bootnodesNode.isArray()) {
                bootnodesNode.forEach(node -> {
                    try {
                        bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64(node.get("enr").asText()));
                    } catch (Exception e) {
                        logger.error("Error reading bootnode", e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Error reading bootnodes.json", e);
        }
    }

    public List<NodeRecord> getBootnodes() {
        return bootnodes;
    }
    
}