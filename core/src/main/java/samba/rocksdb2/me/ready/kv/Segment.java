package samba.rocksdb2.me.ready.kv;

public interface Segment {

    String getName();

    byte[] getId();

    /**
     * Define if this segment contains data that is never updated, but only added and optionally
     * deleted. Example is append only data like the blockchain. This information can be used by the
     * underlying implementation to apply specific optimization for this use case.
     *
     * @return true if the segment contains only static data
     */
    boolean containsStaticData();

    /**
     * This flag defines which segment is eligible for the high spec flag, so basically what column
     * family is involved with high spec flag
     *
     * @return true if the segment is involved with the high spec flag
     */
    boolean isEligibleToHighSpecFlag();

    /**
     * Enable garbage collection for static data. This should be enabled for static data which can be
     * deleted or pruned.
     *
     * @return true if enabled, false otherwise.
     */
    default boolean isStaticDataGarbageCollectionEnabled() {
        return false;
    }
}
