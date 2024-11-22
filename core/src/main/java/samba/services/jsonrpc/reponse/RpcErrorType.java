/*
 * Copyright contributors to Hyperledger Besu.
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
package samba.services.jsonrpc.reponse;

import org.hyperledger.besu.plugin.services.rpc.RpcMethodError;

import java.util.Optional;
import java.util.function.Function;

import static samba.services.jsonrpc.reponse.RpcMethodError.INVALID_PARAMS_ERROR_CODE;

public enum RpcErrorType implements RpcMethodError {

    PARSE_ERROR(-32700, "Parse error"),
    INVALID_REQUEST(-32600, "Invalid Request"),
    METHOD_NOT_FOUND(-32601, "Method not found"),
    INVALID_PARAMS(-32602, "Invalid params"),
    UNKNOWN(INVALID_PARAMS_ERROR_CODE, "Unknown internal error"),
    INVALID_ID_PARAMS(INVALID_PARAMS_ERROR_CODE, "Invalid ID params"),
    INTERNAL_ERROR(-32603, "Internal error"),
    METHOD_NOT_ENABLED(-32604, "Method not enabled"),
    TIMEOUT_ERROR(-32603, "Timeout expired"),
    EXCEEDS_RPC_MAX_BATCH_SIZE(-32005, "Number of requests exceeds max batch size"),
    INVALID_METHOD_PARAMS(INVALID_PARAMS_ERROR_CODE, "Invalid method params");

    private final int code;
    private final String message;
    private final Function<String, Optional<String>> dataDecoder;

    RpcErrorType(final int code, final String message) {
        this(code, message, null);
    }

    RpcErrorType(
            final int code, final String message, final Function<String, Optional<String>> dataDecoder) {
        this.code = code;
        this.message = message;
        this.dataDecoder = dataDecoder;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Optional<String> decodeData(final String data) {
        return dataDecoder == null ? Optional.empty() : dataDecoder.apply(data);
    }
}
