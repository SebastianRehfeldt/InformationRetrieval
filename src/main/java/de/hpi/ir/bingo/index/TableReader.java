package de.hpi.ir.bingo.index;

import java.nio.file.Path;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.google.common.collect.Maps;

final class TableReader<T> implements AutoCloseable {

    private final Input reader;
    private final Class<T> clazz;
    private final Kryo kryo = TableUtil.getKryo();

    public TableReader(Path file, Class<T> clazz) {
        this.clazz = clazz;
        reader = TableUtil.createInput(file);
    }

    public Map.Entry<String, T> readNext() {
        if (!reader.canReadInt()) {
            return null;
        }
        String key = reader.readString();
        T value = kryo.readObject(reader, clazz);
        return Maps.immutableEntry(key, value);
    }

    @Override
    public void close() {
        reader.close();
    }
}
