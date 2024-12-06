/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package samba.timeout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import samba.jsonrpc.config.ContextKey;
import samba.jsonrpc.config.TimeoutOptions;
import samba.jsonrpc.handler.TimeoutHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

public class TimeoutHandlerTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {Optional.empty(), "test_0", DEFAULT_OPTS.getTimeoutSeconds(), true},
          {Optional.empty(), "test_1", DEFAULT_OPTS.getTimeoutSeconds(), false},
          {Optional.of(DEFAULT_OPTS), "test_2", DEFAULT_OPTS.getTimeoutSeconds(), true}
        });
  }

  private static final TimeoutOptions DEFAULT_OPTS = TimeoutOptions.defaultOptions();

  @ParameterizedTest
  @MethodSource("data")
  public void test(
      final Optional<TimeoutOptions> globalOptions,
      final String method,
      final long timeoutSec,
      final boolean timerMustBeSet) {
    final Map<String, TimeoutOptions> options;
    if (timerMustBeSet) {
      options =
          ImmutableMap.of(method, new TimeoutOptions(timeoutSec, DEFAULT_OPTS.getErrorCode()));
    } else {
      options = Collections.emptyMap();
    }
    final Handler<RoutingContext> handler = TimeoutHandler.handler(globalOptions, options);
    final RoutingContext ctx = Mockito.spy(RoutingContext.class);
    final Vertx vertx = Mockito.spy(Vertx.class);

    final JsonObject requestBody = Mockito.mock(JsonObject.class);
    when(requestBody.getString("method")).thenReturn(method);
    when(ctx.data()).thenReturn(Map.of(ContextKey.REQUEST_BODY_AS_JSON_OBJECT.name(), requestBody));
    when(ctx.get(ContextKey.REQUEST_BODY_AS_JSON_OBJECT.name())).thenReturn(requestBody);

    when(ctx.vertx()).thenReturn(vertx);
    handler.handle(ctx);
    verify(vertx, times(timerMustBeSet ? 1 : 0))
        .setTimer(eq(TimeUnit.SECONDS.toMillis(timeoutSec)), any());
    verify(ctx, times(timerMustBeSet ? 1 : 0)).addBodyEndHandler(any());
  }

  @Test
  void dryRunDetector() {
    assertThat(true)
        .withFailMessage("This test is here so gradle --dry-run executes this class")
        .isTrue();
  }
}
