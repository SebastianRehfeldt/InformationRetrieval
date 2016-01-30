package de.hpi.ir.bingo.index;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;

import java.nio.file.Path;
import java.util.Map;

public final class TableReader<T> implements AutoCloseable {

	private final Input reader;
	private final Path file;
	private final Class<T> clazz;
	private final Serializer<T> serializer;
	private final Kryo kryo = TableUtil.getKryo();

	public TableReader(Path file, Class<T> clazz, Serializer<T> serializer) {
		this.file = file;
		this.clazz = clazz;
		this.serializer = TableUtil.getDefaultSerializerIfNull(serializer, clazz);
		reader = TableUtil.createBigBufferInput(file);
	}

	public Map.Entry<String, T> readNext() {
		if (!reader.canReadInt()) {
			return null;
		}
		String key = reader.readString();
		T value = kryo.readObject(reader, clazz, serializer);
		return Maps.immutableEntry(key, value);
	}

	@Override
	public void close() {
		reader.close();
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("file", file)
				.add("clazz", clazz.getSimpleName())
				.add("position", reader.total())
				.toString();
	}
}
