package samba.rocksdb;

import java.nio.charset.StandardCharsets;

// TODO these must be on each sub-network module but validated on this module.
public enum KeyValueSegment implements Segment {
  DEFAULT("default".getBytes(StandardCharsets.UTF_8)),
  BLOCK_HEADER(new byte[] {14}),
  BLOCK_BODY(new byte[] {14}),
  RECEIPT(new byte[] {14}),
  BLOCK_HASH_BY_BLOCK_NUMBER(new byte[] {14}),
  EPHEMERAL_BLOCK_HEADER(new byte[] {14});

  private final byte[] id;
  private final boolean containsStaticData;
  private final boolean eligibleToHighSpecFlag;
  private final boolean staticDataGarbageCollectionEnabled;

  KeyValueSegment(final byte[] id) {
    this(id, false, false, false);
  }

  KeyValueSegment(
      final byte[] id,
      final boolean containsStaticData,
      final boolean eligibleToHighSpecFlag,
      final boolean staticDataGarbageCollectionEnabled) {
    this.id = id;
    this.containsStaticData = containsStaticData;
    this.eligibleToHighSpecFlag = eligibleToHighSpecFlag;
    this.staticDataGarbageCollectionEnabled = staticDataGarbageCollectionEnabled;
  }

  @Override
  public String getName() {
    return name();
  }

  @Override
  public byte[] getId() {
    return id;
  }

  @Override
  public boolean containsStaticData() {
    return containsStaticData;
  }

  @Override
  public boolean isEligibleToHighSpecFlag() {
    return eligibleToHighSpecFlag;
  }

  @Override
  public boolean isStaticDataGarbageCollectionEnabled() {
    return staticDataGarbageCollectionEnabled;
  }
}
