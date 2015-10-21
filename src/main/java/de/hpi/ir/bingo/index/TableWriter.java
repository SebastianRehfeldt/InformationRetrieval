package de.hpi.ir.bingo.index;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

public class TableWriter<T> implements AutoCloseable {

    private final FileOutputStream fos;
    private final ObjectOutputStream writer;
    private final ObjectOutputStream indexWriter;
    private String lastKey = null;

    private final ArrayList<String> indexKeys;
    private final ArrayList<Long> indexPositions;
    private final int bucketSize;
    private int size;

    public TableWriter(Path file, int bucketSize, boolean createIndex) {
        this.bucketSize = bucketSize;
        this.size = 0;
        fos = TableUtil.createOutputStream(file);
        writer = TableUtil.objectOutputStream(fos);
        if (createIndex) {
            indexKeys = Lists.newArrayList();
            indexPositions = Lists.newArrayList();
            indexWriter = TableUtil.objectOutputStream(TableUtil.createOutputStream(TableUtil.getIndexPath(file)));
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
        try {
            if (indexKeys != null && size % bucketSize == 0) {
                writer.flush();
                long position = fos.getChannel().position();
                indexKeys.add(key);
                indexPositions.add(position);
            }
            writer.writeUTF(key);
            writer.writeObject(value);
            size++;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            if (indexKeys != null) {
                TableIndex index = new TableIndex(indexKeys.toArray(new String[indexKeys.size()]), Longs.toArray(indexPositions), bucketSize);
                indexWriter.writeObject(index);
                indexWriter.close();
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
