package de.hpi.ir.bingo.index;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Map;

import com.google.common.collect.Maps;

public class TableReader<T> implements AutoCloseable {

    private final ObjectInputStream reader;

    public TableReader(Path file) {
        reader = TableUtil.objectInputStream(TableUtil.createInputStream(file));
    }

    public Map.Entry<String, T> readNext() {
        try {
            String key = reader.readUTF();
            T value = (T) reader.readObject();
            return Maps.immutableEntry(key, value);
        } catch (EOFException e) {
            return null;
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
