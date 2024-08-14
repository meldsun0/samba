package samba.app.dht;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import samba.domain.dht.Bucket;
import samba.domain.dht.BucketEntry;

import java.util.List;

public class BucketEntryTests {





    @Test
    public void testAddingBucketEntry() {
        final Bucket bucket  = new Bucket(10);
        final List<BucketEntry>  bucketEntries =  TestHelper.createBucketEntryList(4);
        bucketEntries.forEach(bucket::add);
        BucketEntry firstBucketEntry = bucketEntries.getFirst();
        assertEquals(firstBucketEntry, bucket.get(firstBucketEntry.getId()).get());

    }
}
