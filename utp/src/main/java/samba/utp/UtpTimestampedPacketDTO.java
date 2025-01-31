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
package samba.utp;

import samba.utp.data.UtpPacket;

/**
 * Simple DTO class that stores some meta data about a {@see UtpPacket}
 *
 * @author Ivan Iljkic (i.iljkic@gmail.com)
 */
public class UtpTimestampedPacketDTO {

  private Long timestamp;
  private final UtpPacket utpPacket;
  private final int utpTimeStamp;
  private int ackedAfterMeCounter = 0;
  private boolean isPacketAcked = false;
  private boolean reduceWindow;

  private boolean resendBecauseSkipped;
  private int resendCounter = 0;

  public void incrementResendCounter() {
    resendCounter++;
  }

  public int getResendCounter() {
    return resendCounter;
  }

  public UtpTimestampedPacketDTO(UtpPacket u, Long s, int utpStamp) {
    this.timestamp = s;
    this.utpPacket = u;
    this.utpTimeStamp = utpStamp;
  }

  public Long stamp() {
    return timestamp;
  }

  public void setStamp(long stamp) {
    this.timestamp = stamp;
  }

  public UtpPacket utpPacket() {
    return utpPacket;
  }

  public int utpTimeStamp() {
    return utpTimeStamp;
  }

  public boolean isPacketAcked() {
    return isPacketAcked;
  }

  public void setPacketAcked(boolean isPacketAcked) {
    this.isPacketAcked = isPacketAcked;
  }

  public int getAckedAfterMeCounter() {
    return ackedAfterMeCounter;
  }

  public void setAckedAfterMeCounter(int ackedAfterMeCounter) {
    this.ackedAfterMeCounter = ackedAfterMeCounter;
  }

  public void incrementAckedAfterMe() {
    ackedAfterMeCounter++;
  }

  public boolean reduceWindow() {
    return reduceWindow;
  }

  public void setReduceWindow(boolean val) {
    reduceWindow = val;
  }

  public boolean alreadyResendBecauseSkipped() {
    return resendBecauseSkipped;
  }

  public void setResendBecauseSkipped(boolean value) {
    resendBecauseSkipped = value;
  }
}
