package samba.services.discovery;

import samba.network.NetworkType;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bootnodes {

  private static final Logger LOG = LoggerFactory.getLogger(Bootnodes.class);

  public static List<NodeRecord> loadBootnodes(final NetworkType networkType) {
    List<NodeRecord> bootnodes = new ArrayList<>();

    try (InputStream inputStream = Bootnodes.class.getResourceAsStream("/bootnodes.json")) {
      if (inputStream == null) {
        LOG.info("bootnodes.json not found on classpath");
        return bootnodes;
      }

      ObjectMapper objectMapper = new ObjectMapper();
      JsonNode rootNode = objectMapper.readTree(inputStream);
      JsonNode bootnodesNode = rootNode.get(networkType.getName());

      if (bootnodesNode != null && bootnodesNode.isArray()) {
        bootnodesNode.forEach(
            node -> {
              try {
                bootnodes.add(NodeRecordFactory.DEFAULT.fromBase64(node.get("enr").asText()));
              } catch (Exception e) {
                LOG.info("Error reading bootnode entry", e);
              }
            });
      } else {
        LOG.error("No array found for network: {}", networkType.getName());
      }
    } catch (Exception e) {
      LOG.error("Error loading bootnodes.json", e);
    }
    return bootnodes;
  }
}
