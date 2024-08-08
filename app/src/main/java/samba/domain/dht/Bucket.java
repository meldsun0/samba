package samba.domain.dht;

import samba.domain.dht.BucketNode;

/**
 * Head = recently accessed node
 * Tail = least recently accessed node
 */
public class Bucket {
    private final BucketNode[] bucketNodes;
    private final int bucketSize;

    public Bucket(int bucketSize) {
        this.bucketSize = bucketSize;
        this.bucketNodes = new BucketNode[bucketSize];
    }


}
