package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import oshi.hardware.HardwareAbstractionLayer;

public class StartupLogConfig {
  private final String network;

  private final String maxHeapSize;
  private final String memory;
  private final int cpuCores;

  public StartupLogConfig(final String network, final HardwareAbstractionLayer hardwareInfo) {
    this.network = network;

    this.maxHeapSize = normalizeSize(Runtime.getRuntime().maxMemory());
    this.memory = normalizeSize(hardwareInfo.getMemory().getTotal());
    this.cpuCores = hardwareInfo.getProcessor().getLogicalProcessorCount();
  }

  private String normalizeSize(final long size) {
    return String.format("%.02f", (double) size / 1024 / 1024 / 1024) + " GB";
  }

  public List<String> getReport() {
    final String general = String.format("Configuration | Network: %s", network);
    final String host =
        String.format(
            "Host Configuration | Maximum Heap Size: %s, Total Memory: %s, CPU Cores: %d",
            maxHeapSize, memory, cpuCores);
    return List.of(general, host);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String network;
    private String storageMode;
    private HardwareAbstractionLayer hardwareInfo;

    private Builder() {}

    public StartupLogConfig build() {
      return new StartupLogConfig(network, hardwareInfo);
    }

    public Builder network(final String network) {
      checkNotNull(network);
      this.network = network;
      return this;
    }

    public Builder hardwareInfo(final HardwareAbstractionLayer hardwareInfo) {
      checkNotNull(hardwareInfo);
      this.hardwareInfo = hardwareInfo;
      return this;
    }
  }
}
