package samba.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

@CommandLine.Command(
    name = "portal-client",
    mixinStandardHelpOptions = true,
    version = "Portal Client 1.0",
    description = "Java Portal Network Client")
public class SambaCommand implements Runnable {
  private static final Logger logger = LoggerFactory.getLogger(SambaCommand.class);

  @CommandLine.Option(
      names = {"--verbose-logging"},
      description = "Enable Verbose Logging")
  private boolean verboseLogging;

  @CommandLine.Option(
      names = {"--portal-host"},
      description = "Portal Host")
  private String portalHost;

  @CommandLine.Option(
      names = {"--portal-port"},
      description = "Portal Port")
  private String portalPort;

  @CommandLine.Option(
      names = {"--portal-interface"},
      description = "Portal Interface")
  private String portalInterface;

  @CommandLine.Option(
      names = {"--min-peers"},
      description = "Min Peers")
  private int minPeers;

  @CommandLine.Option(
      names = {"--max-peers"},
      description = "Max Peers")
  private int maxPeers;

  @CommandLine.Option(
      names = {"--network"},
      description = "Network Name (mainnet or angelfood)")
  private String networkName;

  @Override
  public void run() {
    logger.info("Command Line");
  }
}
