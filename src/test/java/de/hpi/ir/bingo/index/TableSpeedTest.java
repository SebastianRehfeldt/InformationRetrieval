package de.hpi.ir.bingo.index;

import com.google.common.base.Stopwatch;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

import static com.google.common.truth.Truth.assertThat;


public final class TableSpeedTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testIndexSpeed() throws IOException {
		Path tmpFile = folder.newFile().toPath();

		int insertCount = 100_000;
		int getCount = 100_000;
		int blockSize = 4096;

		String value = "QWERTZUPASDFGHJKLYXCVBNMQWERTZUIOPASDFGHJKLASDASDASDASDASDASDASD";
		Stopwatch sw = Stopwatch.createStarted();
		TableWriter<String> writer = new TableWriter<>(tmpFile, true, blockSize, String.class, null);
		for (int i = 0; i < insertCount; i++) {
			writer.put(String.format("key-%10d", i), value + i);
		}
		writer.close();
		System.out.println("writing table and index: " + sw);
		System.out.println("Table size: " + tmpFile.toFile().length() / 1000_000 + "MB");
		System.out.println("Index size: " + TableUtil.getIndexPath(tmpFile).toFile().length() / 1000 + "KB");

		sw.reset().start();
		Table<String> table = Table.open(tmpFile, String.class, null);
		System.out.println("reading index: " + sw);
		sw.reset().start();
		Random random = new Random(42);
		for (int i = 0; i < getCount; i++) {
			boolean missing = random.nextBoolean();
			String key = String.format("key-%10d", random.nextInt(insertCount)) + (missing ? "XX" : "");
			String result = table.get(key);
			if (!missing) {
				assertThat(result).named(key).isNotNull();
			} else {
				assertThat(result).isNull();
			}

		}
		System.out.println("queries: " + sw);
		table.close();
	}

}