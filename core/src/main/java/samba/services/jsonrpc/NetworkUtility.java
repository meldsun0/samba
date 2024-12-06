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

import java.net.*;

public class NetworkUtility {

  public static final String INADDR_ANY = "0.0.0.0";
  public static final String INADDR_NONE = "255.255.255.255";
  public static final String INADDR6_ANY = "0:0:0:0:0:0:0:0";
  public static final String INADDR6_NONE = "::";

  private NetworkUtility() {}

  public static String urlForSocketAddress(final String scheme, final InetSocketAddress address) {
    String hostName = address.getHostName();
    if (isUnspecifiedAddress(hostName)) {
      hostName = InetAddress.getLoopbackAddress().getHostName();
    }
    if (hostName.contains(":")) {
      hostName = "[" + hostName + "]";
    }
    return scheme + "://" + hostName + ":" + address.getPort();
  }

  public static boolean isValidPort(final int port) {
    return port > 0 && port < 65536;
  }

  public static boolean isUnspecifiedAddress(final String ipAddress) {
    return INADDR_ANY.equals(ipAddress)
        || INADDR6_ANY.equals(ipAddress)
        || INADDR_NONE.equals(ipAddress)
        || INADDR6_NONE.equals(ipAddress);
  }
}
