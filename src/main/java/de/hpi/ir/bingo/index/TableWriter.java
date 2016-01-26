package de.hpi.ir.bingo.index;

import com.google.common.base.Verify;
import com.google.common.collect.Lists;
import com.google.common.primitives.Longs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Output;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TableWriter<T> implements AutoCloseable {

	public static final int BLOCK_SIZE = 2048;
	private final Output writer;
	private final Output indexWriter;
	private final boolean createIndex;
	private final ArrayList<String> indexKeys;
	private final ArrayList<Long> indexPositions;
	private final int blockSize;
	private final Serializer<T> serializer;
	private final Kryo kryo = TableUtil.getKryo();
	private String lastKey = null;
	private long lastIndexPos;
	private int size;

	public TableWriter(Path file, boolean createIndex, Class<T> clazz, Serializer<T> serializer) {
		this(file, createIndex, BLOCK_SIZE, clazz, serializer);
	}

	TableWriter(Path file, boolean createIndex, int blockSize, Class<T> clazz, Serializer<T> serializer) {
		this.blockSize = blockSize;
		this.serializer = TableUtil.getDefaultSerializerIfNull(serializer, clazz);
		lastIndexPos = -blockSize;
		this.createIndex = createIndex;
		this.writer = TableUtil.createOutput(file);

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
		size++;
		long position = writer.total();
		writer.writeString(key);
		kryo.writeObject(writer, value, serializer);
		if (createIndex && writer.total() - lastIndexPos > blockSize) {
			indexKeys.add(key);
			indexPositions.add(position);
			lastIndexPos = position;
		}
	}

	public void writeMap(Map<String, T> index) {
		List<Map.Entry<String, T>> data = new ArrayList<>(index.entrySet());
		data.sort(Map.Entry.comparingByKey());
		for (Map.Entry<String, T> entry : data) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void close() {
		if (indexKeys != null) {
			indexPositions.add(writer.total()); // store length of file
			TableIndex index = new TableIndex(indexKeys.toArray(new String[indexKeys.size()]), Longs.toArray(indexPositions),size);
			kryo.writeObject(indexWriter, index);
			indexWriter.close();
		}
		writer.close();
	}


}
