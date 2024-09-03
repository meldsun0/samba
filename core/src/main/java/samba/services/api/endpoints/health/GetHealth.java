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

package samba.services.api.endpoints.health;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tech.pegasys.teku.infrastructure.restapi.endpoints.EndpointMetadata;
import tech.pegasys.teku.infrastructure.restapi.endpoints.RestApiEndpoint;
import tech.pegasys.teku.infrastructure.restapi.endpoints.RestApiRequest;

import java.util.Optional;

import static tech.pegasys.teku.infrastructure.http.HttpStatusCodes.*;

import static tech.pegasys.teku.infrastructure.http.RestApiConstants.TAG_NODE;

public class GetHealth extends RestApiEndpoint {

    private static final Logger LOG = LogManager.getLogger();
    public static final String ROUTE = "/health";


    public GetHealth() {
        super(
                EndpointMetadata.get(ROUTE)
                        .operationId("getHealth")
                        .summary("Get health check")
                        .description("Returns node health status in http status codes")
                        .tags(TAG_NODE)
                        .response(SC_OK, "Node is ready")
                        .withBadRequestResponse(Optional.of("Invalid syncing status code"))
                        .build());

    }

    @Override
    public void handleRequest(final RestApiRequest request) throws JsonProcessingException {
        request.respondWithCode(SC_OK);

    }


}
