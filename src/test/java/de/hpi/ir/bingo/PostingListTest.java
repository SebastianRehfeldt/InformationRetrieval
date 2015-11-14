package de.hpi.ir.bingo;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PostingListTest {

	PostingList postingList1;
	PostingList postingList2;

	@Before
	public void setup() {
		postingList1 = new PostingList();
		postingList1.addItem(new PostingListItem(1, new int[]{3, 5},10));
		postingList1.addItem(new PostingListItem(2, new int[]{4, 5},10));
		postingList1.addItem(new PostingListItem(3, new int[]{3},10));

		postingList2 = new PostingList();
		postingList2.addItem(new PostingListItem(1, new int[]{4, 5, 7},10));
		postingList2.addItem(new PostingListItem(2, new int[]{3, 4},10));
		postingList2.addItem(new PostingListItem(4, new int[]{3, 4, 5},10));
	}

	@Test
	public void testCombinePhrase() throws Exception {
		PostingList result = postingList1.combinePhrase(postingList2);

		PostingList expectedPostingList = new PostingList();
		expectedPostingList.addItem(new PostingListItem(1, new int[]{4},10));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testAnd() throws Exception {
		PostingList result = postingList1.and(postingList2);

		PostingList expectedPostingList = new PostingList();
		expectedPostingList.addItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10));
		expectedPostingList.addItem(new PostingListItem(2, new int[]{3, 4, 5},10));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testOr() throws Exception {
		PostingList result = postingList1.or(postingList2);

		PostingList expectedPostingList = new PostingList();
		expectedPostingList.addItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10));
		expectedPostingList.addItem(new PostingListItem(2, new int[]{3, 4, 5},10));
		expectedPostingList.addItem(new PostingListItem(3, new int[]{3},10));
		expectedPostingList.addItem(new PostingListItem(4, new int[]{3, 4, 5},10));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testNot() throws Exception {
		PostingList result = postingList1.not(postingList2);

		PostingList expectedPostingList = new PostingList();
		expectedPostingList.addItem(new PostingListItem(3, new int[]{3},10));

		assertThat(result).isEqualTo(expectedPostingList);
	}

}
