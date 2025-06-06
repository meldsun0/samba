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
package samba.jsonrpc.reponse;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface JsonRpcMethod {

  Logger LOG = LoggerFactory.getLogger(JsonRpcMethod.class);

  /**
   * Standardized JSON-RPC method name.
   *
   * @return identification of the JSON-RPC method.
   */
  String getName();

  /**
   * Applies the method to given request.
   *
   * @param requestContext input data for the JSON-RPC method.
   * @return output from applying the JSON-RPC method to the input.
   */
  JsonRpcResponse response(JsonRpcRequestContext requestContext);

  /**
   * The list of Permissions that correspond to this JSON-RPC method.
   *
   * <p>e.g. [*:*, net:*, net:listening]
   *
   * @return list of permissions that match this method.
   */
  default List<String> getPermissions() {
    List<String> permissions = new ArrayList<>();
    permissions.add("*:*");
    permissions.add(this.getName().substring(0, this.getName().indexOf('_')) + ":*");
    permissions.add(this.getName().replace('_', ':'));
    return permissions;
  }

  default JsonRpcErrorResponse createJsonRpcInvalidRequestResponse(
      JsonRpcRequestContext requestContext) {
    return createJsonRpcInvalidRequestResponse(requestContext, RpcErrorType.INVALID_REQUEST);
  }

  default JsonRpcErrorResponse createJsonRpcInvalidRequestResponse(
      JsonRpcRequestContext requestContext, RpcErrorType errorType) {
    return new JsonRpcErrorResponse(requestContext.getRequest().getId(), errorType);
  }

  default JsonRpcResponse createSuccessResponse(
      JsonRpcRequestContext requestContext, final Object result) {
    return new JsonRpcSuccessResponse(requestContext.getRequest().getId(), result);
  }

  default void logJSON(Object value) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      LOG.info(objectMapper.writeValueAsString(value));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
}
