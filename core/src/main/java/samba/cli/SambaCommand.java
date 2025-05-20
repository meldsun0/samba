package samba.cli;

import samba.Samba;
import samba.config.InvalidConfigurationException;
import samba.config.SambaConfiguration;
import samba.config.StorageConfig;
import samba.logging.LogConfigurator;
import samba.network.NetworkType;
import samba.samba.exceptions.ExceptionUtil;
import samba.services.discovery.Bootnodes;
import samba.storage.DatabaseStorageException;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "portal-client",
    mixinStandardHelpOptions = true,
    version = "Portal Client 1.0",
    description = "Java Portal Network Client")
public class SambaCommand implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(SambaCommand.class);

  private final PrintWriter outputWriter;
  private final PrintWriter errorWriter;
  private final Map<String, String> environment;

  private final Samba.StartAction startAction;

  @Option(
      names = {"--unsafe-private-key"},
      description = "Private Key of the local ENR.  If not specified a generated one will be used")
  private String unsafePrivateKey = null;

  @Option(
      names = {"--portal-subnetworks"},
      description = "Portal Subnetwork")
  private String portalSubNetwork = "history-network";

  @Option(
      names = {"--use-default-bootnodes"},
      paramLabel = "<BOOLEAN>",
      showDefaultValue = CommandLine.Help.Visibility.ALWAYS,
      description = "Enables bootnodes",
      fallbackValue = "true",
      arity = "0..1")
  private boolean useDefaultBootnodes = true;

  @Option(
      names = {"--p2p-ip", "--p2p-ip-ips"},
      paramLabel = "<NETWORK>",
      description =
          "P2P IP address(es). You can define up to 2 addresses, with one being IPv4 and the other IPv6",
      split = ",",
      arity = "1..2")
  private List<String> p2pIps = null;

  @Option(
      names = {"--jsonrpc-port"},
      paramLabel = "<INTEGER>",
      description = "Json-Rpc Port",
      arity = "1")
  private Integer jsonRpcPort = null;

  @Option(
      names = {"--jsonrpc-host"},
      description = "Jsonrpc Host",
      arity = "1")
  private String jsonRpcHost = null;

  @CommandLine.Option(
      names = {"--data-path"},
      paramLabel = "<PATH>",
      description = "The path to Samba data directory",
      arity = "1")
  final Path dataPath = StorageConfig.getDefaultSambaDataPath(this);

  @Option(
      names = "--disable-json-rpc-server",
      description = "Disables JSON-RPC Server (set to true if flag is present)",
      defaultValue = "false",
      fallbackValue = "true")
  private boolean disableJsonRpcServer;

  @Option(
      names = "--disable-rest--server",
      description = "Disables REST Server (set to true if flag is present)",
      defaultValue = "false",
      fallbackValue = "true")
  private boolean disableRestServer;

  @Option(
      names = {"--p2p-advertised-ip", "--p2p-advertised-ips"},
      paramLabel = "<NETWORK>",
      description =
          "P2P advertised IP address(es). You can define up to 2 addresses, with one being IPv4 and the other IPv6. (Default: 127.0.0.1)",
      split = ",",
      arity = "1..2")
  private List<String> p2pAdvertisedIps;

  @CommandLine.Option(
      names = {"--logging", "-l"},
      paramLabel = "<LOG VERBOSITY LEVEL>",
      description = "Logging verbosity levels: OFF, ERROR, WARN, INFO, DEBUG, TRACE, ALL",
      defaultValue = "INFO")
  private String loggingLevel = "INFO";

  public SambaCommand(
      final PrintWriter outputWriter,
      final PrintWriter errorWriter,
      final Map<String, String> environment,
      final Samba.StartAction startAction) {
    this.outputWriter = outputWriter;
    this.errorWriter = errorWriter;
    this.environment = environment;
    this.startAction = startAction;
  }

  @Override
  public Integer call() {
    try {
      final SambaConfiguration sambaConfig = sambaConfiguration();
      startAction.start(sambaConfig);
      return 0;
    } catch (final Throwable t) {
      return handleExceptionAndReturnExitCode(t);
    }
  }

  protected SambaConfiguration sambaConfiguration() {
    try {
      SambaConfiguration.Builder builder = SambaConfiguration.builder();

      builder.discovery(
          discoveryConfig -> {
            if (useDefaultBootnodes) {
              discoveryConfig.bootnodes(
                  Bootnodes.loadBootnodes(NetworkType.fromString(portalSubNetwork)));
            }
            if (p2pIps != null) {
              discoveryConfig.networkInterfaces(p2pIps);
            }
            if (p2pAdvertisedIps != null) {
              discoveryConfig.advertisedIps(p2pAdvertisedIps);
            }
          });
      builder.storage(
          storageConfig -> {
            if (dataPath != null) {
              storageConfig.dataPath(dataPath);
            }
          });
      builder.jsonRpc(
          jsonRpc -> {
            jsonRpc.enableJsonRpcServer(!disableJsonRpcServer);
            if (jsonRpcPort != null) {
              jsonRpc.port(jsonRpcPort);
            }
            if (jsonRpcHost != null) {
              jsonRpc.host(jsonRpcHost);
            }
          });
      builder.restServer(restServer -> restServer.enableRestServer(!disableRestServer));

      if (unsafePrivateKey != null) {
        builder.secretKey(unsafePrivateKey);
      }
      builder.useDefaultBootnodes(this.useDefaultBootnodes);
      builder.portalSubNetwork(this.portalSubNetwork);
      builder.loggingLevel(this.loggingLevel);
      configureLogging();
      return builder.build();
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new InvalidConfigurationException(e);
    }
  }

  public int handleExceptionAndReturnExitCode(final Throwable e) {
    final Optional<Throwable> maybeUserErrorException =
        ExceptionUtil.<Throwable>getCause(e, InvalidConfigurationException.class)
            .or(() -> ExceptionUtil.getCause(e, DatabaseStorageException.class));
    if (maybeUserErrorException.isPresent()) {
      LOG.error(e.getMessage(), e);
      return 2;
    } else {
      LOG.error("Samba failed to start", e);
      return 1;
    }
  }

  public int parse(final String[] args) {
    // TODO are more logic regarding parms
    CommandLine commandLine = new CommandLine(this);
    commandLine.parseArgs(args);
    commandLine.setOut(outputWriter);
    commandLine.setErr(errorWriter);
    commandLine.setParameterExceptionHandler(this::handleParseException);
    return commandLine.execute(args);
  }

  private int handleParseException(final CommandLine.ParameterException ex, final String[] args) {
    errorWriter.println(ex.getMessage());
    CommandLine.UnmatchedArgumentException.printSuggestions(ex, errorWriter);
    return ex.getCommandLine().getCommandSpec().exitCodeOnInvalidInput();
  }

  public void configureLogging() {
    Set<String> ACCEPTED_VALUES = Set.of("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE", "ALL");
    if (ACCEPTED_VALUES.contains(this.loggingLevel.toUpperCase(Locale.ROOT))) {
      LogConfigurator.setLevel("", this.loggingLevel);
    }
  }
}
