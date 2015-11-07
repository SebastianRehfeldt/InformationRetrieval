package de.hpi.ir.bingo;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import java.nio.file.Path;

import org.junit.Test;

import com.google.common.collect.Maps;

import de.hpi.ir.bingo.index.TableReader;
import de.hpi.ir.bingo.index.TableWriter;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class PostingListTest {

	@Test
	public void testUnion() throws Exception {
		PostingList postingList1 = new PostingList();
		postingList1.addItem(new PostingListItem(1, new int[]{3,5}));
		postingList1.addItem(new PostingListItem(2, new int[]{4,5}));
		postingList1.addItem(new PostingListItem(3, new int[]{3}));

		PostingList postingList2 = new PostingList();
		postingList2.addItem(new PostingListItem(1, new int[]{4,5,7}));
		postingList2.addItem(new PostingListItem(2, new int[]{3,4}));
		postingList2.addItem(new PostingListItem(4, new int[]{3,4,5}));
		
		PostingList result = postingList1.union(postingList2);
		
		PostingList expectedPostingList = new PostingList();
		expectedPostingList.addItem(new PostingListItem(1,new int[]{4}));
		
		assertThat(result).isEqualTo(expectedPostingList);
	}

}
