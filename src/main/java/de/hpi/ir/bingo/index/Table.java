package de.hpi.ir.bingo.index;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;

import java.nio.file.Path;

public final class Table<T> implements AutoCloseable {

	private final RandomAccessInput input;
	private final TableIndex index;
	private final Class<T> clazz;
	private final Serializer<T> serializer;
	private final Kryo kryo = TableUtil.getKryo();

	private Table(RandomAccessInput input, TableIndex index, Class<T> clazz, Serializer<T> serializer) {
		this.input = input;
		this.index = index;
		this.clazz = clazz;
		this.serializer = TableUtil.getDefaultSerializerIfNull(serializer, clazz);
	}

	public static <T> Table<T> open(Path file, Class<T> clazz, Serializer<T> serializer) {
		Path indexPath = TableUtil.getIndexPath(file);
		Input indexReader = TableUtil.createInput(indexPath);
		TableIndex index = TableUtil.getKryo().readObject(indexReader, TableIndex.class);
		indexReader.close();
		RandomAccessInput input = TableUtil.createInput(file);
		return new Table<>(input, index, clazz, serializer);
	}

	public T get(String key) {
		TableIndex.Range range = index.getRange(key);
		int blockSize = (int) (range.to - range.from);
		input.setStreamPosition(range.from);
		while (input.total() < blockSize) {
			String currentKey = input.readString();
			if (key.equals(currentKey)) {
				return kryo.readObject(input, clazz, serializer);
			} else {
				kryo.readObject(input, clazz, serializer); // skip this
			}
		}
		return null;
	}

	@Override
	public void close() {
		input.close();
	}
}
