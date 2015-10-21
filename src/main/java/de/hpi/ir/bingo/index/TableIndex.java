package de.hpi.ir.bingo.index;

import java.io.Serializable;
import java.util.Arrays;

public class TableIndex implements Serializable {
    private final String[] keys;
    private final long[] positions;
    private final int bucketSize;

    public TableIndex(String[] keys, long[] positions, int bucketSize) {
        this.keys = keys;
        this.positions = positions;
        this.bucketSize = bucketSize;
    }

    public long getPosition(String key) {
        int index = Arrays.binarySearch(keys, key);
        if (index < 0) {
            index = -(index + 2); // binarySearch returns the negative next index -1 if it is not found
        }
        return positions[index];
    }

    public int getBucketSize() {
        return bucketSize;
    }
}
