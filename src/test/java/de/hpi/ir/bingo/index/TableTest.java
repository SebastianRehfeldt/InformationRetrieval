package de.hpi.ir.bingo.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;


public final class TableTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testIndexCreation() throws IOException {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<String> writer = new TableWriter<>(tmpFile, true, 1, String.class, null);
		writer.put("k1", "v1");
		writer.put("k2", "v2");
		writer.close();

		Table<String> table = Table.open(tmpFile, String.class, null);
		assertThat(table.get("k2")).isEqualTo("v2");
		assertThat(table.get("k1")).isEqualTo("v1");
		assertThat(table.get("k11")).isNull();
		assertThat(table.get("k22")).isNull();
		table.close();
	}

	@Test
	public void testIndexCreationWithBuckets() throws IOException {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<String> writer = new TableWriter<>(tmpFile, true, 16, String.class, null);
		writer.put("k1", "v1");
		writer.put("k2", "v2");
		writer.put("k3", "v3");
		writer.put("k4", "v4");
		writer.put("k5", "v5");
		writer.close();

		Table<String> table = Table.open(tmpFile, String.class, null);
		assertThat(table.get("k1")).isEqualTo("v1");
		assertThat(table.get("k2")).isEqualTo("v2");
		assertThat(table.get("k22")).isNull();
		assertThat(table.get("k3")).isEqualTo("v3");
		assertThat(table.get("k4")).isEqualTo("v4");
		assertThat(table.get("k5")).isEqualTo("v5");
		assertThat(table.get("k6")).isNull();
		table.close();
	}

	@Test
	public void testGetWithPrefix() throws IOException {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<String> writer = new TableWriter<>(tmpFile, true, 4096, String.class, null);
		writer.put("k1", "v1");
		writer.put("k2", "v2");
		writer.put("k234", "v3");
		writer.put("k3", "v4");
		writer.close();

		Table<String> table = Table.open(tmpFile, String.class, null);
		assertThat(table.getWithPrefix("k1")).isEqualTo(ImmutableList.of(Maps.immutableEntry("k1", "v1")));
		assertThat(table.getWithPrefix("k2")).isEqualTo(ImmutableList.of(
				Maps.immutableEntry("k2", "v2"), Maps.immutableEntry("k234", "v3")));
		assertThat(table.getWithPrefix("k3")).isEqualTo(ImmutableList.of(Maps.immutableEntry("k3", "v4")));
		table.close();
	}

}