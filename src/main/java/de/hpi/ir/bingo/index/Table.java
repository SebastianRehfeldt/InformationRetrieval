package de.hpi.ir.bingo.index;

import java.nio.file.Path;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

public final class Table<T> implements AutoCloseable {

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
        TableIndex.Range range = index.getRange(key);
        int blockSize = (int) (range.to - range.from);
        input.setStreamPosition(range.from);
        while (input.total() < blockSize) {
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
