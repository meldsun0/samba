/*
 * Copyright Consensys Software Inc., 2022
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

package samba;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import samba.config.SambaConfiguration;

import samba.config.ServiceConfig;
import samba.metrics.MetricsEndpoint;
import samba.node.Node;

import samba.services.PortalNodeServiceController;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.async.OccurrenceCounter;


import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import tech.pegasys.teku.infrastructure.events.EventChannels;


import static tech.pegasys.teku.infrastructure.time.SystemTimeProvider.SYSTEM_TIME_PROVIDER;


public class PortalNode implements Node {

    private static final Logger LOG = LogManager.getLogger();

    private final Vertx vertx = Vertx.vertx();
    private final ExecutorService threadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("events-%d").build());

    private final OccurrenceCounter rejectedExecutionCounter = new OccurrenceCounter(120);
    private Optional<Cancellable> counterMaintainer = Optional.empty();

    private final EventChannels eventChannels;

    private final MetricsEndpoint metricsEndpoint;

    //async actions
    private final AsyncRunnerFactory asyncRunnerFactory;
    private final PortalNodeServiceController serviceController;

    protected PortalNode(final SambaConfiguration sambaConfiguration) {
        this.metricsEndpoint = new MetricsEndpoint(sambaConfiguration.metricsConfig(), vertx);
        this.eventChannels = new EventChannels(new PortalDefaultExceptionHandler(), metricsEndpoint.getMetricsSystem());
        asyncRunnerFactory = AsyncRunnerFactory.createDefault(new MetricTrackingExecutorFactory(metricsEndpoint.getMetricsSystem(), rejectedExecutionCounter));

        final ServiceConfig serviceConfig =
                new ServiceConfig(
                        asyncRunnerFactory,
                        SYSTEM_TIME_PROVIDER,
                        eventChannels,
                        metricsEndpoint.getMetricsSystem(),
                        rejectedExecutionCounter::getTotalCount);


        this.serviceController = new PortalNodeServiceController(sambaConfiguration, serviceConfig);


        // final String network = tekuConfig.eth2NetworkConfiguration().getEth2Network().map(Eth2Network::configName).orElse("empty");
        //final BeaconRestApiConfig beaconChainRestApiConfig = tekuConfig.beaconChain().beaconRestApiConfig();
        //


    }


    @Override
    public void start() {
        this.serviceController.start().join();
//      counterMaintainer =
//              Optional.of(
//                      serviceConfig
//                              .createAsyncRunner("RejectedExecutionCounter", 1)
//                              .runWithFixedDelay(
//                                      this::pollRejectedExecutions,
//                                      Duration.ofSeconds(5),
//                                      (err) -> LOG.debug("rejected execution poll failed", err)));
    }


    @Override
    public void stop() {
        this.eventChannels
                .stop()
                .orTimeout(30, TimeUnit.SECONDS)
                .handleException(error -> LOG.warn("Failed to stop event channels cleanly", error))
                .join();
        threadPool.shutdownNow();
        counterMaintainer.ifPresent(Cancellable::cancel);
        asyncRunnerFactory.shutdown();

        // Stop services.
        this.serviceController
                .stop()
                .orTimeout(30, TimeUnit.SECONDS)
                .handleException(error -> LOG.error("Failed to stop services", error))
                .orTimeout(5, TimeUnit.SECONDS)
                .handleException(error -> LOG.debug("Failed to stop metrics", error))
                .thenRun(vertx::close)
                .join();
    }
}



