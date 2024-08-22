package samba.domain.dht;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;

import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

import org.apache.tuweni.bytes.Bytes;


/**
 * This class represents an array of BucketEntries ordered by their last activity where the head of the array is the
 * recently accessed entry whereas the tail is the least recently accessed entry.
 */
public class Bucket {
    private final BucketEntry[] bucketEntries;
    private final int bucketSize;
    private int capacityIndex = -1;

    public Bucket(int bucketSize) {
        this.bucketSize = bucketSize;
        this.bucketEntries = new BucketEntry[bucketSize];
    }

    /**
     * Add BucketEntry to the head of the bucket array.
     * @param bucketEntry to be added to this list
     * @throws IllegalArgumentException The bucketEntry already existed in the bucket.
     */
    public synchronized void add(@NonNull BucketEntry bucketEntry)  throws IllegalArgumentException {
        checkCapacityReached();
        checkDuplicateInsertion(bucketEntry);
        arraycopy(this.bucketEntries, 0, this.bucketEntries, 1, ++capacityIndex);
        this.bucketEntries[0] = bucketEntry;
    }


    /**
     * Returns the bucketEntry with the provided ID if it exists in the bucket and
     * relocates it to the head of the bucketEntry[].
     *
     * @param id The bucketEntry's ID.
     * @return An option with the bucketEntry found or an empty optional if the bucketEntry was not found
     */
    public synchronized Optional<BucketEntry> get(@NonNull final Bytes id) {
        for (int i = 0; i <= this.capacityIndex; i++) {
            final BucketEntry bucketEntry = this.bucketEntries[i];
            if (bucketEntry.getId().equals(id)) {
                arraycopy(this.bucketEntries, 0, this.bucketEntries, 1, i);
                bucketEntries[0] = bucketEntry;
                return Optional.of(bucketEntry);
            }
        }
        return Optional.empty();
    }

    /**
     * Removes the BucketEntry by moving bucketEntries to the left.
     *
     * @param bucketEntry the element to be removed
     * @return <code>true</code>
     */
   public synchronized boolean remove(@NonNull  final BucketEntry bucketEntry) {
        if (this.capacityIndex < 0)  return false;
        for (int i = 0; i <= capacityIndex; i++) {
            if (bucketEntry.getId().equals(this.bucketEntries[i].getId())) {
                arraycopy(this.bucketEntries, i + 1, this.bucketEntries, i, capacityIndex - i);
                this.bucketEntries[capacityIndex--] = null;
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an unmodifiable list  of the bucketEntries[]
     * @return An unmodifiable list
     */
    public synchronized List<BucketEntry> getAll() {
        return unmodifiableList(asList(Arrays.copyOf(this.bucketEntries, capacityIndex + 1)));
    }

    @Override
    public String toString() {
        return Arrays.toString(this.bucketEntries);
    }

    private synchronized void checkDuplicateInsertion(@NonNull final BucketEntry bucketEntry) {
        if (Arrays.asList(this.bucketEntries).contains(bucketEntry)) {
            throw new IllegalArgumentException(String.format("Duplicate Insertion on BucketEntry: %s", bucketEntry.getId()));
        }
    }

    private synchronized void checkCapacityReached() {
        if (capacityIndex >= bucketSize -1) {
            throw new IllegalArgumentException(String.format("Capacity reached on BucketEntry"));
        }
    }
}