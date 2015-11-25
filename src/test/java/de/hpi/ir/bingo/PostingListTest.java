package de.hpi.ir.bingo;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

import de.hpi.ir.bingo.queries.QueryResultItem;
import de.hpi.ir.bingo.queries.QueryResultList;

public class PostingListTest {

	QueryResultList postingList1;
	QueryResultList postingList2;

	@Before
	public void setup() {
		postingList1 = new QueryResultList();
		postingList1.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 5},10),1));
		postingList1.addItem(new QueryResultItem(new PostingListItem(2, new int[]{4, 5},10),1));
		postingList1.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10),1));

		postingList2 = new QueryResultList();
		postingList2.addItem(new QueryResultItem(new PostingListItem(1, new int[]{4, 5, 7},10),1));
		postingList2.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4},10),1));
		postingList2.addItem(new QueryResultItem(new PostingListItem(4, new int[]{3, 4, 5},10),1));
	}

	@Test
	public void testCombinePhrase() throws Exception {
		QueryResultList result = postingList1.combinePhrase(postingList2);

		QueryResultList expectedPostingList = new QueryResultList();
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{4},10),1));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testAnd() throws Exception {
		QueryResultList result = postingList1.and(postingList2);

		QueryResultList expectedPostingList = new QueryResultList();
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10),1));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4, 5},10),1));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testOr() throws Exception {
		QueryResultList result = postingList1.or(postingList2);

		QueryResultList expectedPostingList = new QueryResultList();
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10),1));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4, 5},10),1));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10),1));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(4, new int[]{3, 4, 5},10),1));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testNot() throws Exception {
		QueryResultList result = postingList1.not(postingList2);

		QueryResultList expectedPostingList = new QueryResultList();
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10), 1));

		assertThat(result).isEqualTo(expectedPostingList);
	}

}
