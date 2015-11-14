package de.hpi.ir.bingo;

import com.google.common.collect.ImmutableList;

import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.hpi.ir.bingo.index.TableUtil;

import it.unimi.dsi.fastutil.ints.IntArrayList;

import org.junit.Rule;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;

@RunWith(Theories.class)
public class PostingListSerializerTest {

	@DataPoint
	public static Serializer compressingSerializer = PostingList.COMPRESSING_SERIALIZER;

	@DataPoint
	public static Serializer serializer = PostingList.NORMAL_SERIALIZER;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Theory
	public void testSerialize(Serializer serializer) throws Exception {
		Path tmpFile = folder.newFile().toPath();
		Output output = TableUtil.createOutput(tmpFile);

		PostingListItem i1 = new PostingListItem(5, new IntArrayList(new int[]{3, 7, 9}),10);
		PostingListItem i2 = new PostingListItem(12, new IntArrayList(new int[]{5, 8}),10);
		PostingListItem i3 = new PostingListItem(27, new IntArrayList(new int[]{9, 10}),10);
		PostingList pl = new PostingList(ImmutableList.of(i1, i2, i3));

		TableUtil.getKryo().writeObject(output, pl, serializer);
		output.close();

		Input input = TableUtil.createInput(tmpFile);
		PostingList postingList = TableUtil.getKryo().readObject(input, PostingList.class, serializer);

		assertThat(postingList).isEqualTo(pl);
	}
}