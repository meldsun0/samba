package samba.rocksdb;

import java.nio.charset.StandardCharsets;

public enum TestSegment implements Segment {
  DEFAULT("default".getBytes(StandardCharsets.UTF_8)),
  FOO(new byte[] {1}),
  BAR(new byte[] {2}),
  EXPERIMENTAL(new byte[] {3}),

  STATIC_DATA(new byte[] {4}, true, false);

  private final byte[] id;
  private final String nameAsUtf8;
  private final boolean containsStaticData;
  private final boolean eligibleToHighSpecFlag;

  TestSegment(final byte[] id) {
    this(id, false, false);
  }

  TestSegment(
      final byte[] id, final boolean containsStaticData, final boolean eligibleToHighSpecFlag) {
    this.id = id;
    this.nameAsUtf8 = new String(id, StandardCharsets.UTF_8);
    this.containsStaticData = containsStaticData;
    this.eligibleToHighSpecFlag = eligibleToHighSpecFlag;
  }

  @Override
  public String getName() {
    return nameAsUtf8;
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
}
