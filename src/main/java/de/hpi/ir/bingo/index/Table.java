package de.hpi.ir.bingo.index;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.PostingList;

public final class Table<T> implements AutoCloseable {

	private final RandomAccessInput input;
	private final TableIndex index;
	private final Class<T> clazz;
	private final Serializer<T> serializer;
	private final Kryo kryo = TableUtil.getKryo();
	private final LoadingCache<String, T> cache = CacheBuilder.newBuilder().softValues().build(CacheLoader.from(this::get));

	private Table(RandomAccessInput input, TableIndex index, Class<T> clazz, Serializer<T> serializer) {
		this.input = input;
		this.index = index;
		this.clazz = clazz;
		this.serializer = TableUtil.getDefaultSerializerIfNull(serializer, clazz);
	}


	public static <T> Table<T> open(Path file, Class<T> clazz, Serializer<T> serializer) {
		TableIndex index = TableUtil.getTableIndex(file);
		RandomAccessInput input = TableUtil.createRandomAccessInput(file);
		return new Table<>(input, index, clazz, serializer);
	}


	public T getCached(String key) {
		try {
			return cache.get(key);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
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

	public List<Map.Entry<String, T>> getWithPrefix(String key) {
		List<Map.Entry<String, T>> result = Lists.newArrayList();
		TableIndex.Range range = index.getRange(key);
		long remainingFileSize = index.getFileSize() - range.from;
		input.setStreamPosition(range.from);
		while (input.total() < remainingFileSize) {
			String currentKey = input.readString();
			if (currentKey.compareTo(key) < 0) {
				kryo.readObject(input, clazz, serializer); // skip this
			} else if (currentKey.startsWith(key)) {
				result.add(Maps.immutableEntry(currentKey, kryo.readObject(input, clazz, serializer)));
			} else {
				break;
			}
		}
		return result;
	}


	@Override
	public void close() {
		input.close();
	}

	public int getSize() {
		return index.getSize();
	}
}
