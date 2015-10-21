package de.hpi.ir.bingo.index;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;

public class Table<T> implements AutoCloseable {

    public static final long MISSING_ENTRY = -1L;
    private final FileInputStream fis;
    private final ObjectInputStream reader;
    private final TableIndex index;

    private Table(FileInputStream fis, TableIndex index) {
        this.fis = fis;
        this.reader = TableUtil.objectInputStream(fis);
        this.index = index;
    }

    public T get(String key) {
        long position = index.getPosition(key);
        if (position == MISSING_ENTRY) {
            return null;
        }
        try {
            fis.getChannel().position(position);
            for (int i = 0; i < index.getBucketSize(); i++) {
                String currentKey = reader.readUTF();
                if (key.equals(currentKey)) {
                    return (T) reader.readObject();
                } else {
                    reader.readObject(); // skip this
                }
            }
            return null;
        } catch (EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Table<T> open(Path file) {
        Path indexFile = TableUtil.getIndexPath(file);
        ObjectInputStream indexReader = TableUtil.objectInputStream(TableUtil.createInputStream(indexFile));
        FileInputStream fis = TableUtil.createInputStream(file);
        try {
            TableIndex index = (TableIndex) indexReader.readObject();
            indexReader.close();
            return new Table<T>(fis, index);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
