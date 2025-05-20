package samba.config;

import java.util.ArrayList;
import java.util.List;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;

public class StartupHardwareConfig {
  private final String maxHeapSize;
  private final String memory;
  private final int cpuCores;
  private final HardwareAbstractionLayer hardwareInfo;

  public StartupHardwareConfig() {
    this.hardwareInfo = new SystemInfo().getHardware();
    this.maxHeapSize = normalizeSize(Runtime.getRuntime().maxMemory());
    this.memory = normalizeSize(hardwareInfo.getMemory().getTotal());
    this.cpuCores = hardwareInfo.getProcessor().getLogicalProcessorCount();
  }

  private String normalizeSize(final long size) {
    return String.format("%.02f", (double) size / 1024 / 1024 / 1024) + " GB";
  }

  public List<String> getStartupSummeryLog() {
    List<String> summary = new ArrayList<>();
    summary.add(
        "Host Hardware Summary: "
            + String.format(
                "Max Heap: %s, Total Memory: %s, CPU cores: %d", maxHeapSize, memory, cpuCores));
    return summary;
  }
}
