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

package samba.rocksdb.keyvalue;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import samba.rocksdb.serialization.*;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;


import java.util.Set;

public interface KvStoreSerializer<T> {
    KvStoreSerializer<UInt64> UINT64_SERIALIZER = new UInt64Serializer();
    KvStoreSerializer<Bytes> BYTES_SERIALIZER = new BytesSerializer<>(Bytes::wrap);
    KvStoreSerializer<Bytes32> BYTES32_SERIALIZER = new BytesSerializer<>(Bytes32::wrap);
    KvStoreSerializer<Set<Bytes32>> BLOCK_ROOTS_SERIALIZER = new Bytes32SetSerializer();
    KvStoreSerializer<Void> VOID_SERIALIZER = new VoidSerializer();


    T deserialize(final byte[] data);

    byte[] serialize(final T value);
}
