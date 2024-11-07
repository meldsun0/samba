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

package samba.rocksdb;

import org.apache.tuweni.bytes.Bytes;
import samba.rocksdb.keyvalue.KvStoreSerializer;

import java.util.Objects;

import static tech.pegasys.teku.infrastructure.unsigned.ByteUtil.toByteExact;

public class KvStoreVariable<TValue> {
  private final Bytes id;
  private final KvStoreSerializer<TValue> serializer;

  private KvStoreVariable(final byte[] id, final KvStoreSerializer<TValue> serializer) {
    this.id = Bytes.wrap(id);
    this.serializer = serializer;
  }

  public static <T> KvStoreVariable<T> create(final int id, final KvStoreSerializer<T> serializer) {
    final byte byteId = toByteExact(id);
    return new KvStoreVariable<T>(new byte[] {byteId}, serializer);
  }

  public Bytes getId() {
    return id;
  }

  public KvStoreSerializer<TValue> getSerializer() {
    return serializer;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final KvStoreVariable<?> that = (KvStoreVariable<?>) o;
    return Objects.equals(id, that.id) && Objects.equals(serializer, that.serializer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, serializer);
  }
}
