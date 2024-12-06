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
  private static final Logger logger = LoggerFactory.getLogger(Bootnodes.class);

  public static List<NodeRecord> loadBootnodes(final NetworkType networkType) {
    InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("bootnodes.json");
    List<NodeRecord> bootnodes = new ArrayList<>();
    if (inputStream != null) {
      ObjectMapper objectMapper = new ObjectMapper();
      try {
        JsonNode rootNode = objectMapper.readTree(inputStream);
        JsonNode bootnodesNode = rootNode.get(networkType.getName());
        if (bootnodesNode.isArray()) {
          bootnodesNode.forEach(
              node -> {
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
    return bootnodes;
  }
}
