package samba.domain.content;

import samba.schema.content.ssz.blockbody.BlockBodyPostShanghaiContainer;
import samba.schema.content.ssz.blockbody.BlockBodyPreShanghaiContainer;

public class ContentBlockBody {

  private final BlockBodyPreShanghaiContainer blockBodyPreShanghaiContainer;
  private final BlockBodyPostShanghaiContainer blockBodyPostShanghaiContainer;
  private final long timestamp;

  public ContentBlockBody(
      BlockBodyPreShanghaiContainer blockBodyPreShanghaiContainer, long timestamp) {
    this.blockBodyPreShanghaiContainer = blockBodyPreShanghaiContainer;
    this.blockBodyPostShanghaiContainer = null;
    this.timestamp = timestamp;
  }

  public ContentBlockBody(
      BlockBodyPostShanghaiContainer blockBodyPostShanghaiContainer, long timestamp) {
    this.blockBodyPreShanghaiContainer = null;
    this.blockBodyPostShanghaiContainer = blockBodyPostShanghaiContainer;
    this.timestamp = timestamp;
  }
}
