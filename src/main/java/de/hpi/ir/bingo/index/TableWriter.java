package de.hpi.ir.bingo.index;

import java.nio.file.Path;
import java.util.ArrayList;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

public class TableWriter<T> implements AutoCloseable {

    private final Output writer;
    private final Output indexWriter;
    private String lastKey = null;

    private final ArrayList<String> indexKeys;
    private final ArrayList<Long> indexPositions;
    private final int bucketSize;
    private int size;
    private final Kryo kryo = new Kryo();

    public TableWriter(Path file, int bucketSize, boolean createIndex) {
        this.bucketSize = bucketSize;
        this.size = 0;
        writer = TableUtil.createOutput(file);
        if (createIndex) {
            indexKeys = Lists.newArrayList();
            indexPositions = Lists.newArrayList();
            indexWriter = TableUtil.createOutput(TableUtil.getIndexPath(file));
        } else {
            indexKeys = null;
            indexPositions = null;
            indexWriter = null;
        }
    }

    public void put(String key, T value) {
        Verify.verifyNotNull(key);
        Verify.verify(lastKey == null || lastKey.compareTo(key) < 0, "please insert in order!");
        lastKey = key;
            if (indexKeys != null && size % bucketSize == 0) {
                long position = writer.total();
                indexKeys.add(key);
                indexPositions.add(position);
            }
            writer.writeAscii(key);
            kryo.writeObject(writer, value);
            size++;
    }

    @Override
    public void close() {
        if (indexKeys != null) {
            TableIndex index = new TableIndex(indexKeys.toArray(new String[indexKeys.size()]), Longs.toArray(indexPositions), bucketSize);
            kryo.writeObject(indexWriter, index);
            indexWriter.close();
        }
        writer.close();
    }
}
