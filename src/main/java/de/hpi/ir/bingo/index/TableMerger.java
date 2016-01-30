package de.hpi.ir.bingo.index;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import com.esotericsoftware.kryo.Serializer;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public final class TableMerger {

	private static class Holder<T extends Mergeable<T>> implements Comparable<Holder<T>> {
		final String key;
		T value;
		final int index;

		public Holder(String key, T item, int value) {
			this.key = key;
			this.value = item;
			this.index = value;
		}

		@Override
		public int compareTo(Holder<T> o) {
			return ComparisonChain.start().compare(key, o.key).compare(value, o.value).result();
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
					.add("key", key)
					.add("value", value)
					.add("index", index)
					.toString();
		}
	}

	public static <T extends Mergeable<T>> void merge(Path merged, List<Path> files, Class<T> clazz, Serializer<T> serializer) {
		TableWriter<T> writer = new TableWriter<>(merged, true, 4096, clazz, serializer);
		PriorityQueue<Holder<T>> queue = new PriorityQueue<>();
		List<TableReader<T>> readers = Lists.newArrayList();
		for (int i = 0; i < files.size(); i++) {
			TableReader<T> reader = new TableReader<>(files.get(i), clazz, serializer);
			readers.add(reader);
			readNext(queue, readers, i);
		}

		Holder<T> entry = queue.poll();
		readNext(queue, readers, entry.index);

		while (entry != null) {
			Holder<T> next = queue.poll();
			if (next != null) {
				readNext(queue, readers, next.index);
			}
			while (next != null && entry.key.equals(next.key)) {
				entry = new Holder<T>(entry.key, entry.value.mergedWith(next.value), -1);
				next = queue.poll();
				if (next != null) {
					readNext(queue, readers, next.index);
				}
			}
			writer.put(entry.key, entry.value);
			entry = next;
		}

		writer.close();
		for (TableReader<T> reader : readers) {
			Verify.verify(reader.readNext() == null, "reader did not complete: " + reader.toString()  );
			reader.close();
		}
	}

	private static <T extends Mergeable<T>> void readNext(PriorityQueue<Holder<T>> queue, List<TableReader<T>> readers, int index) {
		Map.Entry<String, T> entry = readers.get(index).readNext();
		if (entry != null) {
			Holder<T> holder = new Holder<T>(entry.getKey(), entry.getValue(), index);
			queue.add(holder);
		}
	}


	public interface Mergeable<T> extends Comparable<T> {
		/**
		 * return a copy that is this object merged with {@code other}
		 */
		T mergedWith(T other);
	}
}
