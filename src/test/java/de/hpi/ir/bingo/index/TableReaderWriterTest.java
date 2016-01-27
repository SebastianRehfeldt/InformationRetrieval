package de.hpi.ir.bingo.index;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;

public final class TableReaderWriterTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();


	@Test
	public void testReadNext() throws Exception {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<String> writer = new TableWriter<>(tmpFile, false, 1, String.class, null);
		writer.put("k1", "v1");
		writer.put("k2", "v2");
		writer.close();

		TableReader<String> reader = new TableReader<>(tmpFile, String.class, null);
		assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k1", "v1"));
		assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k2", "v2"));
		assertThat(reader.readNext()).isNull();
		reader.close();
	}

	@Test
	public void testWritePostingList() throws Exception {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<PostingList> writer = new TableWriter<>(tmpFile, false, 1, PostingList.class, PostingList.NORMAL_SERIALIZER);
		PostingList list = new PostingList();
		list.addItem(new PostingListItem(5, 10, (short) 10, (short) 0));
		writer.put("k1", list);
		writer.close();

		TableReader<PostingList> reader = new TableReader<>(tmpFile, PostingList.class, PostingList.NORMAL_SERIALIZER);
		Map.Entry<String, PostingList> entry = reader.readNext();
		assertThat(entry).isNotNull();
		assertThat(entry.getKey()).isEqualTo("k1");
		assertThat(entry.getValue()).isEqualTo(list);
		assertThat(reader.readNext()).isNull();
		reader.close();
	}

	@Test
	public void testWriteMap() throws Exception {
		Path tmpFile = folder.newFile().toPath();

		TableWriter<String> writer = new TableWriter<>(tmpFile, false, 1, String.class, null);
		writer.writeMap(ImmutableMap.of("k1", "v1", "k2", "v2"));
		writer.close();

		TableReader<String> reader = new TableReader<>(tmpFile, String.class, null);
		assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k1", "v1"));
		assertThat(reader.readNext()).isEqualTo(Maps.immutableEntry("k2", "v2"));
		assertThat(reader.readNext()).isNull();
		reader.close();
	}


	@Test(expected = VerifyException.class)
	public void testWriterChecksOrder() throws Exception {
		Path tmpFile = folder.newFile().toPath();
		try (TableWriter<String> writer = new TableWriter<>(tmpFile, false, 1, String.class, null)) {
			writer.put("k2", "v1");
			writer.put("k1", "v2");
		}
	}
}