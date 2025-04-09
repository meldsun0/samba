package samba.services.api;

import samba.config.PortalRestApiConfig;
import samba.config.VersionProvider;
import samba.services.api.endpoints.health.GetHealth;

import javax.naming.ServiceUnavailableException;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.events.EventChannels;
import tech.pegasys.teku.infrastructure.http.HttpErrorResponse;
import tech.pegasys.teku.infrastructure.restapi.RestApi;
import tech.pegasys.teku.infrastructure.restapi.RestApiBuilder;
import tech.pegasys.teku.infrastructure.time.TimeProvider;

public class PortalAPI implements PortalRestAPI {

  private final RestApi restApi;

  public PortalAPI(
      final PortalRestApiConfig config,
      final EventChannels eventChannels,
      final AsyncRunner asyncRunner,
      final TimeProvider timeProvider) {
    restApi = create(config, eventChannels, asyncRunner, timeProvider);
  }

  @Override
  public SafeFuture<?> start() {
    return restApi.start();
  }

  @Override
  public SafeFuture<?> stop() {
    return restApi.stop();
  }

  @Override
  public int getListenPort() {
    return restApi.getListenPort();
  }

  private static RestApi create(
      final PortalRestApiConfig config,
      final EventChannels eventChannels,
      final AsyncRunner asyncRunner,
      final TimeProvider timeProvider) {
    //    final SchemaDefinitionCache schemaCache = new SchemaDefinitionCache(spec);
    RestApiBuilder builder =
        new RestApiBuilder()
            .openApiInfo(
                openApi ->
                    openApi
                        .title(StringUtils.capitalize(VersionProvider.CLIENT_IDENTITY))
                        .version(VersionProvider.IMPLEMENTATION_VERSION)
                        .description("A minimal API specification for the Portal node, which ...")
                        .license("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0.html"))
            .openApiDocsEnabled(config.isRestApiDocsEnabled())
            .listenAddress(config.getRestApiInterface())
            .port(config.getRestApiPort())
            .maxUrlLength(config.getMaxUrlLength())
            .corsAllowedOrigins(config.getRestApiCorsAllowedOrigins())
            .hostAllowlist(config.getRestApiHostAllowlist())
            //                        .exceptionHandler(
            //                                NodeSyncingException.class, (throwable) ->
            // HttpErrorResponse.serviceUnavailable())
            .exceptionHandler(
                ServiceUnavailableException.class,
                (throwable) -> HttpErrorResponse.serviceUnavailable())
            //                        .exceptionHandler(
            //                                ContentTypeNotSupportedException.class,
            //                                (throwable) ->
            //                                        new
            // HttpErrorResponse(SC_UNSUPPORTED_MEDIA_TYPE, throwable.getMessage()))
            //                        .exceptionHandler(
            //                                BadRequestException.class,
            //                                (throwable) ->
            // HttpErrorResponse.badRequest(throwable.getMessage()))
            .exceptionHandler(
                JsonProcessingException.class,
                (throwable) -> HttpErrorResponse.badRequest(throwable.getMessage()))
            .exceptionHandler(
                IllegalArgumentException.class,
                (throwable) -> HttpErrorResponse.badRequest(throwable.getMessage()))
            // Beacon Handlers
            // .endpoint(new PostBlockV2(dataProvider, spec, schemaCache))
            // Event Handler
            //                        .endpoint(
            //                                new GetEvents(
            //                                        spec,
            //                                        dataProvider,
            //                                        eventChannels,
            //                                        asyncRunner,
            //                                        timeProvider,
            //                                        config.getMaxPendingEvents()))
            // Node Handlers
            .endpoint(new GetHealth());
    //                        .endpoint(new GetIdentity(dataProvider, spec.getNetworkingConfig()))
    //                        .endpoint(new GetPeers(dataProvider))
    //                        .endpoint(new GetPeerCount(dataProvider))
    //                        .endpoint(new GetPeerById(dataProvider))
    //                        .endpoint(new GetSyncing(dataProvider))
    //                        .endpoint(new GetVersion());

    return builder.build();
  }
}
