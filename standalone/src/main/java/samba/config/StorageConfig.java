package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

public class StorageConfig {

  public static final String DEFAULT_DATA_DIR_PATH = "./build/data";
  private static String SAMBA_HOME_PROPERTY_NAME = "samba.home";

  private final Path dataPath;
  private static final Logger LOG = LoggerFactory.getLogger(StorageConfig.class);

  public StorageConfig(final Path dataPath) {
    this.dataPath = dataPath;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Path getDefaultSambaDataPath(final Object command) {
    final String sambaHomeProperty = System.getProperty(SAMBA_HOME_PROPERTY_NAME);
    final Path sambaHome;
    if (sambaHomeProperty != null) {
      try {
        sambaHome = Paths.get(sambaHomeProperty);
      } catch (final InvalidPathException e) {
        throw new CommandLine.ParameterException(
            new CommandLine(command),
            String.format(
                "Unable to define default data directory from %s property.",
                SAMBA_HOME_PROPERTY_NAME),
            e);
      }
    } else {
      try {
        final String path = new File(DEFAULT_DATA_DIR_PATH).getCanonicalPath();
        sambaHome = Paths.get(path);
      } catch (final IOException e) {
        throw new CommandLine.ParameterException(
            new CommandLine(command), "Unable to create default data directory.");
      }
    }
    try {
      Files.createDirectories(sambaHome);
    } catch (final FileAlreadyExistsException e) {
      throw new CommandLine.ParameterException(
          new CommandLine(command),
          String.format("%s: already exists and is not a directory.", sambaHome.toAbsolutePath()),
          e);
    } catch (final Exception e) {
      throw new CommandLine.ParameterException(
          new CommandLine(command),
          String.format("Error creating directory %s.", sambaHome.toAbsolutePath()),
          e);
    }
    return sambaHome;
  }

  public Path getDataPath() {
    return this.dataPath;
  }

  public static class Builder {

    private Path dataPath;

    private Builder() {}

    public StorageConfig build() {
      return new StorageConfig(dataPath);
    }

    public Builder dataPath(final Path dataPath) {
      checkNotNull(dataPath);
      this.dataPath = dataPath;
      return this;
    }
  }
}
