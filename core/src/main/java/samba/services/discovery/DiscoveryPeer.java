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

package samba.services.discovery;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.tuweni.bytes.Bytes;


import java.net.InetSocketAddress;


public record DiscoveryPeer(Bytes publicKey, InetSocketAddress nodeAddress) {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DiscoveryPeer)) {
            return false;
        }
        DiscoveryPeer that = (DiscoveryPeer) o;
        return Objects.equal(publicKey(), that.publicKey())
                && Objects.equal(nodeAddress(), that.nodeAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                publicKey(),
                nodeAddress());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("publicKey", publicKey)
                .add("nodeAddress", nodeAddress)
                .toString();
    }
}
