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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;


import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.function.Function;


public class NodeRecordConverter {
    private static final Logger LOG = LogManager.getLogger();

    public Optional<DiscoveryPeer> convertToDiscoveryPeer(
            final NodeRecord nodeRecord,
            final boolean supportsIpv6,
            final SchemaDefinitions schemaDefinitions) {
        final Optional<InetSocketAddress> tcpAddress;
        if (supportsIpv6) {
            // prefer IPv6 address
            tcpAddress = nodeRecord.getTcp6Address().or(nodeRecord::getTcpAddress);
        } else {
            tcpAddress = nodeRecord.getTcpAddress();
        }
        return tcpAddress.map(
                address -> socketAddressToDiscoveryPeer(schemaDefinitions, nodeRecord, address));
    }

    private static DiscoveryPeer socketAddressToDiscoveryPeer(
            final SchemaDefinitions schemaDefinitions,
            final NodeRecord nodeRecord,
            final InetSocketAddress address) {

        //TODO parse other attributes.

        return new DiscoveryPeer(
                ((Bytes) nodeRecord.get(EnrField.PKEY_SECP256K1)),
                address);
    }

    private static <T> Optional<T> parseField(
            final NodeRecord nodeRecord, final String fieldName, final Function<Bytes, T> parse) {
        try {
            return Optional.ofNullable((Bytes) nodeRecord.get(fieldName)).map(parse);
        } catch (final Exception e) {
            LOG.debug("Failed to parse ENR field {}", fieldName, e);
            return Optional.empty();
        }
    }
}
