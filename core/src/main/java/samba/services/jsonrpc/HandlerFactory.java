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
package samba.services.jsonrpc;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import samba.services.jsonrpc.config.JsonRpcConfiguration;
import samba.services.jsonrpc.handler.JsonRpcExecutorHandler;
import samba.services.jsonrpc.handler.JsonRpcParserHandler;
import samba.services.jsonrpc.handler.TimeoutHandler;
import samba.services.jsonrpc.reponse.JsonRpcMethod;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HandlerFactory {

    public static Handler<RoutingContext> timeout(
            final TimeoutOptions globalOptions, final Map<String, JsonRpcMethod> methods) {
        assert methods != null && globalOptions != null;
        return TimeoutHandler.handler(
                Optional.of(globalOptions),
                methods.keySet().stream()
                        .collect(Collectors.toMap(Function.identity(), ignored -> globalOptions)));
    }


    public static Handler<RoutingContext> jsonRpcParser() {
        return JsonRpcParserHandler.handler();
    }

    public static Handler<RoutingContext> jsonRpcExecutor(
            final JsonRpcExecutor jsonRpcExecutor,
            final JsonRpcConfiguration jsonRpcConfiguration) {
        return JsonRpcExecutorHandler.handler(jsonRpcExecutor, jsonRpcConfiguration);
    }
}
