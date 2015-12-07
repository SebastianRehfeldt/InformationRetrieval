package de.hpi.ir.bingo.queries;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

import de.hpi.ir.bingo.PostingListItem;

public class QueryResultListTest {

	QueryResultList postingList1;
	QueryResultList postingList2;

	@Before
	public void setup() {
		postingList1 = new QueryResultList();
		postingList1.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 5},10,10),0, null));
		postingList1.addItem(new QueryResultItem(new PostingListItem(2, new int[]{4, 5},10,10),0, null));
		postingList1.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10,10),0, null));

		postingList2 = new QueryResultList();
		postingList2.addItem(new QueryResultItem(new PostingListItem(1, new int[]{4, 5, 7},10,10),0, null));
		postingList2.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4},10,10),0, null));
		postingList2.addItem(new QueryResultItem(new PostingListItem(4, new int[]{3, 4, 5},10,10),0, null));
	}

	@Test
	public void testCombine() throws Exception {
		QueryResultList result = postingList1.combine(postingList2);

		QueryResultList expectedPostingList = new QueryResultList(2);
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10,10),0, null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4, 5},10,10),0, null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10,10), 1*QueryResultList.MISSING_SCORE, null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(4, new int[]{3, 4, 5},10,10), 1*QueryResultList.MISSING_SCORE, null));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testCombinePhrase() throws Exception {
		QueryResultList result = postingList1.combinePhrase(postingList2);

		QueryResultList expectedPostingList = new QueryResultList(1);
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{4},10,10),0, null));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testAnd() throws Exception {
		QueryResultList result = postingList1.and(postingList2);

		QueryResultList expectedPostingList = new QueryResultList(2);
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10,10),0, null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4, 5},10,10),0, null));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testOr() throws Exception {
		QueryResultList result = postingList1.or(postingList2);

		QueryResultList expectedPostingList = new QueryResultList(2);
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(1, new int[]{3, 4, 5, 7},10,10),Math.log(2), null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(2, new int[]{3, 4, 5},10,10),Math.log(2), null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10,10),0, null));
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(4, new int[]{3, 4, 5},10,10),0, null));

		assertThat(result).isEqualTo(expectedPostingList);
	}

	@Test
	public void testNot() throws Exception {
		QueryResultList result = postingList1.not(postingList2);

		QueryResultList expectedPostingList = new QueryResultList();
		expectedPostingList.addItem(new QueryResultItem(new PostingListItem(3, new int[]{3},10,10), 0, null));

		assertThat(result).isEqualTo(expectedPostingList);
	}

}
