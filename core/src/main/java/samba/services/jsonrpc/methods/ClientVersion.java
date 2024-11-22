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
package samba.services.jsonrpc.methods;

import samba.services.jsonrpc.reponse.JsonRpcMethod;
import samba.services.jsonrpc.reponse.JsonRpcRequestContext;
import samba.services.jsonrpc.reponse.JsonRpcResponse;
import samba.services.jsonrpc.reponse.JsonRpcSuccessResponse;
import samba.services.jsonrpc.config.RpcMethod;

public class ClientVersion implements JsonRpcMethod {

  private final String clientVersion;

  public ClientVersion(final String clientVersion) {
    this.clientVersion = clientVersion;
  }

  @Override
  public String getName() {
    return RpcMethod.CLIENT_VERSION.getMethodName();
  }

  @Override
  public JsonRpcResponse response(final JsonRpcRequestContext requestContext) {
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), clientVersion);
  }
}
