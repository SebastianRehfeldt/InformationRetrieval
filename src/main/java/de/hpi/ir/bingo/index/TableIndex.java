package de.hpi.ir.bingo.index;

import com.google.common.base.Preconditions;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.Arrays;

/**
 * TODO!
 */
final class TableIndex {
	private final String[] keys;
	private final long[] positions;

	private TableIndex() {
		this(null, null);
	}

	public TableIndex(String[] keys, long[] positions) {
		Preconditions.checkArgument(keys.length + 1 == positions.length, "please store the length of the file as last position");
		this.keys = keys;
		this.positions = positions;
	}

	public Range getRange(String key) {
		int index = Arrays.binarySearch(keys, key);
		if (index < 0) {
			index = -(index + 2); // binarySearch returns the negative next index -1 if key is not found
		}
		return new Range(positions[index], positions[index + 1]);
	}

	public static class Range {
		public final long from;
		public final long to;

		public Range(long from, long to) {
			this.from = from;
			this.to = to;
		}
	}

	public static class TableIndexSerializer extends Serializer<TableIndex> {
		public void write(Kryo kryo, Output output, TableIndex index) {
			output.writeInt(index.keys.length);
			for (int i = 0; i < index.keys.length; i++) {
				output.writeAscii(index.keys[i]);
			}
			output.writeLongs(index.positions);
		}


		public TableIndex read(Kryo kryo, Input input, Class<TableIndex> type) {
			int length = input.readInt();
			String[] keys = new String[length];
			for (int i = 0; i < keys.length; i++) {
				keys[i] = input.readString();
			}
			long[] positions = input.readLongs(length + 1);
			return new TableIndex(keys, positions);
		}
	}
}
