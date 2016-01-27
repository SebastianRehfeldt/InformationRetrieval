package de.hpi.ir.bingo.index;

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PatentData;
import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;
import it.unimi.dsi.fastutil.ints.IntLists;


public class TableMergerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testMergeSingle() throws Exception {
		List<Path> files = Lists.newArrayList();

		Path tmpFile = folder.newFile().toPath();
		TableWriter<PostingList> writer = new TableWriter<>(tmpFile, false, 1, PostingList.class, PostingList.NORMAL_SERIALIZER);
		PostingList list = getPostingList(1, new int[]{1, 2, 3}, new int[]{11, 12, 13});
		writer.put("k1", list);
		writer.close();
		files.add(tmpFile);

		Path merged = folder.newFile().toPath();
		TableMerger.merge(merged, files, PostingList.class, PostingList.NORMAL_SERIALIZER);
		TableReader<PostingList> reader = new TableReader<>(merged, PostingList.class, PostingList.NORMAL_SERIALIZER);

		Map.Entry<String, PostingList> postingList = reader.readNext();
		assertThat(postingList).isNotNull();
		assertThat(postingList.getKey()).isEqualTo("k1");
		assertThat(postingList.getValue().getDocumentCount()).isEqualTo(2);
	}

	@Test
	public void testMergePatents() throws Exception {
		List<Path> files = Lists.newArrayList();

		Path tmpFile = folder.newFile().toPath();
		TableWriter<PatentData> writer = new TableWriter<>(tmpFile, false, 1, PatentData.class, null);
		writer.put("p1", new PatentData(1, "a", "", "", IntLists.EMPTY_LIST));
		writer.put("p2", new PatentData(2, "a", "", "", IntLists.EMPTY_LIST));
		writer.close();
		files.add(tmpFile);

		tmpFile = folder.newFile().toPath();
		writer = new TableWriter<>(tmpFile, false, 1, PatentData.class, null);
		writer.put("p3", new PatentData(3, "a", "", "", IntLists.EMPTY_LIST));
		writer.put("p4", new PatentData(4, "a", "", "", IntLists.EMPTY_LIST));
		writer.close();
		files.add(tmpFile);

		Path merged = folder.newFile().toPath();
		TableMerger.merge(merged, files, PatentData.class, null);
		TableReader<PatentData> reader = new TableReader<>(merged, PatentData.class, null);

		for (int i = 1; i <= 4; i++) {
			Map.Entry<String, PatentData> patent = reader.readNext();
			assertThat(patent).isNotNull();
			assertThat(patent.getKey()).isEqualTo("p" + i);
		}

	}

	private PostingList getPostingList(int startIndex, int[]... positions) {
		PostingList list = new PostingList();
		for (int[] position : positions) {
			list.addItem(new PostingListItem(startIndex++, position, 10, (short) 10, (short) 0));
		}
		return list;
	}

	@Test
	public void testMerge() throws Exception {
		List<Path> files = Lists.newArrayList();
		Path tmpFile;
		TableWriter<PostingList> writer;
		PostingList list;

		tmpFile = folder.newFile().toPath();
		writer = new TableWriter<>(tmpFile, false, 1, PostingList.class, PostingList.NORMAL_SERIALIZER);
		list = getPostingList(10, new int[]{1, 2, 3}, new int[]{11, 12, 13});
		writer.put("k1", list);
		list = getPostingList(1, new int[]{21, 22, 23}, new int[]{21, 22, 23});
		writer.put("k2", list);
		list = getPostingList(1, new int[]{31, 32, 33}, new int[]{41, 42, 43});
		writer.put("k3", list);
		writer.close();
		files.add(tmpFile);

		tmpFile = folder.newFile().toPath();
		writer = new TableWriter<>(tmpFile, false, 1, PostingList.class, PostingList.NORMAL_SERIALIZER);
		list = getPostingList(3, new int[]{4, 5, 6}, new int[]{1, 2, 3});
		writer.put("k1", list);
		list = getPostingList(3, new int[]{101, 102, 103}, new int[]{111, 112, 113});
		writer.put("k3", list);
		writer.close();
		files.add(tmpFile);

		tmpFile = folder.newFile().toPath();
		writer = new TableWriter<>(tmpFile, false, 1, PostingList.class, PostingList.NORMAL_SERIALIZER);
		list = getPostingList(5, new int[]{7, 8, 9}, new int[]{1, 2, 3});
		writer.put("k1", list);
		writer.close();
		files.add(tmpFile);

		Path merged = folder.newFile().toPath();
		TableMerger.merge(merged, files, PostingList.class, PostingList.NORMAL_SERIALIZER);
		TableReader<PostingList> reader = new TableReader<>(merged, PostingList.class, PostingList.NORMAL_SERIALIZER);

		Map.Entry<String, PostingList> postingList = reader.readNext();
		assertThat(postingList).isNotNull();
		assertThat(postingList.getKey()).isEqualTo("k1");
		assertThat(postingList.getValue().getDocumentCount()).isEqualTo(6);
		List<Integer> indices = postingList.getValue().getItems().stream().map(PostingListItem::getPatentId).collect(Collectors.toList());
		assertThat(indices).containsExactly(3, 4, 5, 6, 10, 11).inOrder();

		postingList = reader.readNext();
		assertThat(postingList).isNotNull();
		assertThat(postingList.getKey()).isEqualTo("k2");
		assertThat(postingList.getValue().getDocumentCount()).isEqualTo(2);

		postingList = reader.readNext();
		assertThat(postingList).isNotNull();
		assertThat(postingList.getKey()).isEqualTo("k3");
		assertThat(postingList.getValue().getDocumentCount()).isEqualTo(4);
	}
}