package samba.domain.dht;

import org.apache.tuweni.bytes.Bytes;

import samba.domain.node.Node;

import java.util.function.Supplier;

/**
 *
 */
public class BucketEntry implements Comparable<BucketEntry> {

    private Node node;

    private long firstTimeAdded;

    private long livenessChecks;

    private boolean isValidated;

    public BucketEntry(Node node){
        this.node = node;

    }


    public Bytes getId(){
        return this.node.getId();
    }
    @Override
    public int compareTo(BucketEntry bucketEntry) {
        return this.node.getId().compareTo(bucketEntry.getId());
    }
}