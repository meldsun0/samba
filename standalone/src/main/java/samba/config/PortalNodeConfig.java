package samba.config;

import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.time.TimeProvider;

public record PortalNodeConfig(
    TimeProvider timeProvider, EventChannels eventChannels, MetricsSystem metricsSystem) {}
