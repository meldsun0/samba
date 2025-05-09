package samba;

import samba.api.Discv5API;

public interface NetworkSDK<T> {

  T getSDK();

  // TODO temp
  Discv5API getDiscv5API();
}
