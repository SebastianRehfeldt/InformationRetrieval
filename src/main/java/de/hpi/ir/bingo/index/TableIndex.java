package de.hpi.ir.bingo.index;

import java.util.Arrays;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

final class TableIndex {
    private final String[] keys;
    private final long[] positions;
    private final int bucketSize;

    private TableIndex() {
        this(null, null, -1);
    }

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


    public static class TableIndexSerializer extends Serializer<TableIndex> {
        public void write(Kryo kryo, Output output, TableIndex index) {
            output.writeInt(index.positions.length);
            for (int i = 0; i < index.keys.length; i++) {
                output.writeAscii(index.keys[i]);
            }
            output.writeLongs(index.positions);
            output.writeInt(index.bucketSize);
        }


        public TableIndex read(Kryo kryo, Input input, Class<TableIndex> type) {
            int length = input.readInt();
            String[] keys = new String[length];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = input.readString();
            }
            long[] positions = input.readLongs(length);
            int bucketSize = input.readInt();
            return new TableIndex(keys, positions, bucketSize);
        }
    }
}
