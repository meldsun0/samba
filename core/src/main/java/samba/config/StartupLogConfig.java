/*
 * Copyright Consensys Software Inc., 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package samba.config;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import oshi.hardware.HardwareAbstractionLayer;

public class StartupLogConfig {
  private final String network;

  private final String maxHeapSize;
  private final String memory;
  private final int cpuCores;

  private final boolean portalNodeRestApiEnabled;
  private final String portalNodeRestApiInterface;
  private final int portalNodeRestApiPort;
  private final List<String> portalNodeRestRestApiAllowList;

  public StartupLogConfig(
      final String network,
      final HardwareAbstractionLayer hardwareInfo,
      final boolean portalNodeRestApiEnabled,
      final String portalNodeRestApiInterface,
      final int portalNodeRestApiPort,
      final List<String> portalNodeRestRestApiAllowList) {
    this.network = network;

    this.maxHeapSize = normalizeSize(Runtime.getRuntime().maxMemory());
    this.memory = normalizeSize(hardwareInfo.getMemory().getTotal());
    this.cpuCores = hardwareInfo.getProcessor().getLogicalProcessorCount();

    this.portalNodeRestApiEnabled = portalNodeRestApiEnabled;
    this.portalNodeRestApiInterface = portalNodeRestApiInterface;
    this.portalNodeRestApiPort = portalNodeRestApiPort;
    this.portalNodeRestRestApiAllowList = portalNodeRestRestApiAllowList;
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
    final String restApi =
        portalNodeRestApiEnabled
            ? String.format(
                "Rest Api Configuration | Enabled: true, Listen Address: %s, Port: %s, Allow: %s",
                portalNodeRestApiInterface, portalNodeRestApiPort, portalNodeRestRestApiAllowList)
            : "Rest Api Configuration | Enabled: false";
    return List.of(general, host, restApi);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String network;
    private String storageMode;
    private HardwareAbstractionLayer hardwareInfo;
    private boolean portalNodeRestApiEnabled;
    private String portalNodeRestApiInterface;
    private int portalNodeRestApiPort;
    private List<String> portalNodeRestRestApiAllowList;

    private Builder() {}

    public StartupLogConfig build() {
      return new StartupLogConfig(
          network,
          hardwareInfo,
          portalNodeRestApiEnabled,
          portalNodeRestApiInterface,
          portalNodeRestApiPort,
          portalNodeRestRestApiAllowList);
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

    public Builder portalNodeRestApiEnabled(final boolean portalNodeRestApiEnabled) {
      this.portalNodeRestApiEnabled = portalNodeRestApiEnabled;
      return this;
    }

    public Builder portalNodeRestApiInterface(final String portalNodeRestApiInterface) {
      checkNotNull(portalNodeRestApiInterface);
      this.portalNodeRestApiInterface = portalNodeRestApiInterface;
      return this;
    }

    public Builder portalNodeRestApiPort(final int portalNodeRestApiPort) {
      this.portalNodeRestApiPort = portalNodeRestApiPort;
      return this;
    }

    public Builder portalNodeRestApiAllowList(final List<String> portalNodeRestRestApiAllowList) {
      checkNotNull(portalNodeRestRestApiAllowList);
      this.portalNodeRestRestApiAllowList = portalNodeRestRestApiAllowList;
      return this;
    }
  }
}
