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

package samba.services;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import samba.config.SambaConfiguration;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.service.serviceutils.Service;
import tech.pegasys.teku.service.serviceutils.ServiceFacade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PortalNodeMainController extends Service {

    private static final Logger LOG = LogManager.getLogger();
    protected final List<Service> services = new ArrayList<>();

    public PortalNodeMainController(final MainServiceConfig mainServiceConfig, SambaConfiguration sambaConfiguration) {
          services.add(new PortalNodeMainService(mainServiceConfig, sambaConfiguration));
    }

    @Override
    protected SafeFuture<?> doStart() {
        LOG.debug("Starting {}", this.getClass().getSimpleName());
        final Iterator<Service> iterator = services.iterator();
        SafeFuture<?> startupFuture = iterator.next().start();
        while (iterator.hasNext()) {
            final Service nextService = iterator.next();
            startupFuture = startupFuture.thenCompose(__ -> nextService.start());
        }
        return startupFuture;
    }

    @Override
    protected SafeFuture<?> doStop() {
        return SafeFuture.allOf(services.stream().map(Service::stop).toArray(SafeFuture[]::new));
    }

    public List<? extends ServiceFacade> getServices() {
        return services;
    }
}
