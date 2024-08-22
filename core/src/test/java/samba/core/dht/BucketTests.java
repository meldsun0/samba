package samba.core.dht;

import org.junit.jupiter.api.Test;
import samba.domain.dht.Bucket;
import samba.domain.dht.BucketEntry;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BucketTests {

    @Test
    public void testAddingBucketEntry() {
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry> bucketEntries =  TestHelper.createBucketEntryList(4);
        bucketEntries.forEach(bucket::add);
        BucketEntry firstBucketEntry = bucketEntries.getFirst();
        BucketEntry bucketLastEntry = bucketEntries.getLast();
        assertEquals(firstBucketEntry, bucket.get(firstBucketEntry.getId()).get());
    }

    @Test
    public void testRemovingBucketEntry() {
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry> bucketEntries =  TestHelper.createBucketEntryList(10);
        bucketEntries.forEach(bucket::add);
        BucketEntry bucketLastEntry = bucketEntries.getLast();

        assertThat(bucket.remove(bucketLastEntry)).isTrue();
        assertThat(bucket.get(bucketLastEntry.getId())).isEmpty();
    }

    @Test
    public void testBucketEntriesBeingRemovedToTheLeftWhileRemovingBucketEntry() {
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry> bucketEntries =  TestHelper.createBucketEntryList(10);
        BucketEntry third = bucketEntries.get(2);
        BucketEntry fourth = bucketEntries.get(3);
        bucketEntries.forEach(bucket::add);

        assertThat(bucket.remove(fourth)).isTrue();
        assertThat(bucket.get(fourth.getId())).isEmpty();
        assertThat(bucket.getAll().get(6)).isEqualTo(third);
    }

    @Test
    public void testDuplicateInsertion(){
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry> bucketEntries =  TestHelper.createBucketEntryList(9);
        BucketEntry entry = bucketEntries.get(0);
        bucketEntries.forEach(bucket::add);

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> bucket.add(entry));
        assertEquals("Duplicate Insertion on BucketEntry: "+entry.getId(), exception.getMessage());
    }

    @Test
    public void testCapacityReached(){
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry> bucketEntries =  TestHelper.createBucketEntryList(10);
        bucketEntries.forEach(bucket::add);
        BucketEntry newEntry = TestHelper.createBucketEntry();

        Throwable exception = assertThrows(IllegalArgumentException.class, () -> bucket.add(newEntry));
        assertEquals("Capacity reached on BucketEntry", exception.getMessage());
    }

}
