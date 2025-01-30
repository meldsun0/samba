/* Copyright 2013 Ivan Iljkic
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package samba.utp.data;

import static samba.utp.data.bytes.UnsignedTypesUtil.longToUbyte;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for uTP Headers.
 *
 * @author Ivan Iljkic (i.iljkic@gmail.com)
 */
public class UtpPacketUtils {

  public static final byte VERSION = longToUbyte(1);

  public static final byte DATA = (byte) (VERSION | longToUbyte(0));
  public static final byte FIN = (byte) (VERSION | longToUbyte(16));
  public static final byte STATE = (byte) (VERSION | longToUbyte(32));
  public static final byte RESET = (byte) (VERSION | longToUbyte(48));
  public static final byte SYN = (byte) (VERSION | longToUbyte(64));

  public static final byte NO_EXTENSION = longToUbyte(0);
  public static final byte SELECTIVE_ACK = longToUbyte(1);

  public static final int MAX_UTP_PACKET_LENGTH = 1500;
  public static final int MAX_UDP_HEADER_LENGTH = 48;
  public static final int DEF_HEADER_LENGTH = 20;

  private static final Logger log = LoggerFactory.getLogger(UtpPacketUtils.class);

  public static byte[] joinByteArray(byte[] array1, byte[] array2) {
    byte[] safeArray1 = array1 == null ? new byte[0] : array1;
    byte[] safeArray2 = array2 == null ? new byte[0] : array2;
    int totalLength = safeArray1.length + safeArray2.length;
    byte[] returnArray = new byte[totalLength];
    System.arraycopy(safeArray1, 0, returnArray, 0, safeArray1.length);
    System.arraycopy(safeArray2, 0, returnArray, safeArray1.length, safeArray2.length);
    return returnArray;
  }
}
