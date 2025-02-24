package samba.utp;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import samba.services.discovery.Discv5Client;
import samba.services.utp.UTPManager;

import java.util.List;

import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UTPManagerTests {

  private UTPManager utpManager;

  @BeforeEach
  public void before() {
    this.utpManager = new UTPManager(mock(Discv5Client.class));
  }

}
