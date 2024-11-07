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

package samba.schema.content;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import samba.rocksdb.KvStoreColumn;
import samba.rocksdb.KvStoreVariable;
import tech.pegasys.teku.ethereum.pow.api.DepositTreeSnapshot;
import tech.pegasys.teku.ethereum.pow.api.DepositsFromBlockEvent;
import tech.pegasys.teku.ethereum.pow.api.MinGenesisTimeBlockEvent;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.spec.datastructures.blocks.BlockCheckpoints;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SchemaCombined extends Schema {


  //0x01 + SSZ.serialize(Container(block_hash: Bytes32))


  // Columns
  KvStoreColumn<Bytes32, SignedBeaconBlock> getColumnHotBlocksByRoot();

  KvStoreColumn<Bytes32, UInt64> getColumnSlotsByFinalizedRoot();

  // Variables
  KvStoreVariable<UInt64> getVariableGenesisTime();


  Map<String, KvStoreColumn<?, ?>> getColumnMap();

  Map<String, KvStoreVariable<?>> getVariableMap();

  @Override
  Collection<KvStoreColumn<?, ?>> getAllColumns();

  @Override
  Collection<KvStoreVariable<?>> getAllVariables();

}
