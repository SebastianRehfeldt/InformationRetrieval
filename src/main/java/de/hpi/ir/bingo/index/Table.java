package de.hpi.ir.bingo.index;

import java.nio.file.Path;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public final class Table<T> implements AutoCloseable {

    public static final long MISSING_ENTRY = -1L;
    private final RandomAccessInput input;
    private final TableIndex index;
    private final Class<T> clazz;
    private final Kryo kryo = TableUtil.getKryo();

    private Table(RandomAccessInput input, TableIndex index, Class<T> clazz) {
        this.input = input;
        this.index = index;
        this.clazz = clazz;
    }

    public T get(String key) {
        int position = (int) index.getPosition(key);
        if (position == MISSING_ENTRY) {
            return null;
        }
        input.setStreamPosition(position);
        for (int i = 0; i < index.getBucketSize(); i++) {
            if (!input.canReadInt()) {
                return null;
            }
            String currentKey = input.readString();
            if (key.equals(currentKey)) {
                return kryo.readObject(input, clazz);
            } else {
                kryo.readObject(input, String.class); // skip this
            }
        }
        return null;
    }

    public static <T> Table<T> open(Path file, Class<T> clazz) {
        Path indexPath = TableUtil.getIndexPath(file);
        Input indexReader = TableUtil.createInput(indexPath);
        TableIndex index = TableUtil.getKryo().readObject(indexReader, TableIndex.class);
        indexReader.close();
        RandomAccessInput input = TableUtil.createInput(file);
        return new Table<>(input, index, clazz);
    }

    @Override
    public void close() {
        input.close();
    }
}
