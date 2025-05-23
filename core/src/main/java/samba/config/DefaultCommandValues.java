package samba.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import picocli.CommandLine;

public interface DefaultCommandValues {

  String SAMBA_HOME_PROPERTY_NAME = "samba.home";
  String DEFAULT_DATA_DIR_PATH = "./build/samba";

  static Path getDefaultSambaDataPath(final Object command) {
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
}
