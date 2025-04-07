package samba.config;

import samba.SambaInitializer;
import samba.exceptions.DatabaseStorageException;
import samba.exceptions.ExceptionUtil;
import samba.network.NetworkType;
import samba.services.discovery.Bootnodes;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private final SambaInitializer.StartAction startAction;

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

  @CommandLine.Option(
      names = {"--data-path"},
      paramLabel = "<PATH>",
      description = "The path to Samba data directory",
      arity = "1")
  final Path dataPath = StorageConfig.getDefaultSambaDataPath(this);

  public SambaCommand(
      final PrintWriter outputWriter,
      final PrintWriter errorWriter,
      final Map<String, String> environment,
      final SambaInitializer.StartAction startAction) {
    this.outputWriter = outputWriter;
    this.errorWriter = errorWriter;
    this.environment = environment;
    this.startAction = startAction;
  }

  @Override
  public Integer call() {
    try {
      final SambaConfig sambaConfig = sambaConfiguration();
      startAction.start(sambaConfig);
      return 0;
    } catch (final Throwable t) {
      return handleExceptionAndReturnExitCode(t);
    }
  }

  protected SambaConfig sambaConfiguration() {
    try {
      SambaConfig.Builder builder = SambaConfig.builder();

      builder.discovery(
          discoveryConfig -> {
            if (useDefaultBootnodes) {
              discoveryConfig.bootnodes(
                  Bootnodes.loadBootnodes(NetworkType.fromString(portalSubNetwork)));
            }
            if (p2pIps != null) {
              discoveryConfig.networkInterfaces(p2pIps);
            }
          });
      builder.storage(
          storageConfig -> {
            if (dataPath != null) {
              storageConfig.dataPath(dataPath);
            }
          });
      if (unsafePrivateKey != null) {
        builder.secretKey(unsafePrivateKey);
      }

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
}
