package de.hpi.ir.bingo.index;

import com.esotericsoftware.kryo.Serializer;

import java.nio.file.Path;
import java.util.Map;

// TODO write tests!
public final class TableMerger {

	public static <T extends Mergeable<T>> void merge(Path merged, Path file1, Path file2, Class<T> clazz, Serializer<T> serializer) {
		TableWriter<T> writer = new TableWriter<>(merged, false, 4096, clazz, serializer);
		TableReader<T> reader1 = new TableReader<>(file1, clazz, serializer);
		TableReader<T> reader2 = new TableReader<>(file2, clazz, serializer);

 		Map.Entry<String, T> entry1 = reader1.readNext();
		Map.Entry<String, T> entry2 = reader2.readNext();

		while (entry1 != null && entry2 != null) {
			String key1 = entry1.getKey();
			String key2 = entry2.getKey();
			int comparison = key1.compareTo(key2);
			if (comparison == 0) {
				writer.put(key1, entry1.getValue().mergedWith(entry2.getValue()));
				entry1 = reader1.readNext();
				entry2 = reader2.readNext();
			} else if (comparison < 0) {
				writer.put(key1, entry1.getValue());
				entry1 = reader1.readNext();
			} else {
				writer.put(key2, entry2.getValue());
				entry2 = reader2.readNext();
			}
		}
		if (entry1 != null) {
			writer.put(entry1.getKey(), entry1.getValue());
		}
		if (entry2 != null) {
			writer.put(entry2.getKey(), entry2.getValue());
		}

		writer.close();
		reader1.close();
		reader2.close();
	}

	public interface Mergeable<T> {


		/**
		 * return a copy that is this object merged with {@code other}
		 */
		T mergedWith(T other);
	}
}
