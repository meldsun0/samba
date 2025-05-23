package samba.cli;

import static java.nio.charset.StandardCharsets.UTF_8;

import samba.Samba;
import samba.config.DefaultCommandValues;
import samba.config.InvalidConfigurationException;
import samba.config.SambaConfiguration;
import samba.logging.LogConfigurator;
import samba.network.NetworkType;
import samba.samba.exceptions.ExceptionUtil;
import samba.services.discovery.Bootnodes;
import samba.storage.DatabaseStorageException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Splitter;
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
  final Path dataPath = DefaultCommandValues.getDefaultSambaDataPath(this);

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

  private String sambaUserName = "samba";

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

      checkPermissions(this.dataPath, sambaUserName, false);

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
              storageConfig.databasePath(dataPath);
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
      builder.dataPath(this.dataPath);
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

  public Path dataDir() {
    return dataPath.toAbsolutePath();
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

  // Helper method to check permissions on a given path
  private void checkPermissions(final Path path, final String sambaUser, final boolean readOnly) {
    try {
      // Get the permissions of the file
      // check if samba user is the owner - get owner permissions if yes
      // else, check if samba user and owner are in the same group - if yes, check the group
      // permission
      // otherwise check permissions for others

      // Get the owner of the file or directory
      UserPrincipal owner = Files.getOwner(path);
      boolean hasReadPermission, hasWritePermission;

      // Get file permissions
      Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(path);

      // Check if samba is the owner
      if (owner.getName().equals(sambaUser)) {
        // Owner permissions
        hasReadPermission = permissions.contains(PosixFilePermission.OWNER_READ);
        hasWritePermission = permissions.contains(PosixFilePermission.OWNER_WRITE);
      } else {
        // Get the group of the file
        // Get POSIX file attributes and then group
        PosixFileAttributes attrs = Files.readAttributes(path, PosixFileAttributes.class);
        GroupPrincipal group = attrs.group();

        // Check if samba user belongs to this group
        boolean isMember = isGroupMember(sambaUserName, group);

        if (isMember) {
          // Group's permissions
          hasReadPermission = permissions.contains(PosixFilePermission.GROUP_READ);
          hasWritePermission = permissions.contains(PosixFilePermission.GROUP_WRITE);
        } else {
          // Others' permissions
          hasReadPermission = permissions.contains(PosixFilePermission.OTHERS_READ);
          hasWritePermission = permissions.contains(PosixFilePermission.OTHERS_WRITE);
        }
      }

      if (!hasReadPermission || (!readOnly && !hasWritePermission)) {
        String accessType = readOnly ? "READ" : "READ_WRITE";
        LOG.info("PERMISSION_CHECK_PATH:{}:{}", path, accessType);
      }
    } catch (Exception e) {
      LOG.error(
          "Error: Failed to check permissions for path: '{}'. Reason: {}", path, e.getMessage());
    }
  }

  private static boolean isGroupMember(final String userName, final GroupPrincipal group)
      throws IOException {
    // Get the groups of the user by executing 'id -Gn username'
    Process process = Runtime.getRuntime().exec(new String[] {"id", "-Gn", userName});
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream(), UTF_8));

    // Read the output of the command
    String line = reader.readLine();
    boolean isMember = false;
    if (line != null) {
      // Split the groups
      Iterable<String> userGroups = Splitter.on(" ").split(line);
      // Check if any of the user's groups match the file's group

      for (String grp : userGroups) {
        if (grp.equals(group.getName())) {
          isMember = true;
          break;
        }
      }
    }
    return isMember;
  }
}
