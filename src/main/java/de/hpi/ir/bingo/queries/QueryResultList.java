package de.hpi.ir.bingo.queries;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;

public final class QueryResultList {
	public static final Comparator<? super QueryResultItem> SCORE_COMPARATOR = Comparator.comparing(QueryResultItem::getScore).reversed();
	public static final Comparator<? super QueryResultItem> ID_COMPARATOR = Comparator.comparing(QueryResultItem::getPatentId).reversed();

	static final double MISSING_SCORE = Math.log(1.0 / 100_000);

	private final List<QueryResultItem> items;

	// number of QueryResultLists that were combined to create this one
	private final int combinations;

	/**
	 * create a single result
	 **/
	public QueryResultList(PostingList postingList) {
		this(Lists.newArrayListWithCapacity(postingList.getItems().size()), 1);
		for (PostingListItem item : postingList.getItems()) {
			items.add(new QueryResultItem(item, 0, null));
		}
	}

	public QueryResultList() {
		this(1);
	}

	public QueryResultList(int combinations) {
		this(Lists.newArrayList(), combinations);
	}

	private QueryResultList(List<QueryResultItem> items, int combinations) {
		this.items = items;
		this.combinations = combinations;
	}

	public void addItem(QueryResultItem item) {
		Verify.verify(items.isEmpty() || items.get(items.size() - 1).getPatentId() < item.getPatentId());
		items.add(item);
	}

	public QueryResultList and(QueryResultList other) {
		Preconditions.checkNotNull(other);
		List<QueryResultItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		QueryResultList result = new QueryResultList(combinations + other.combinations);
		while (i1 < items.size() && i2 < items2.size()) {
			QueryResultItem p1 = items.get(i1);
			QueryResultItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem and = p1.getItem().merge(p2.getItem());
				double score = p1.getScore() + p2.getScore();
				String snippet = p1.getSnippet() != null ? p1.getSnippet() : p2.getSnippet();
				result.addItem(new QueryResultItem(and, score, snippet));
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return result;
	}

	public QueryResultList or(QueryResultList other) {
		return combine(other); // use the same ranking model
	}

	public QueryResultList not(QueryResultList other) {
		Preconditions.checkNotNull(other);
		List<QueryResultItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		QueryResultList result = new QueryResultList(combinations);
		while (i1 < items.size() && i2 < items2.size()) {
			QueryResultItem p1 = items.get(i1);
			QueryResultItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				result.addItem(p1);
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		while (i1 < items.size()) {
			result.addItem(items.get(i1++));
		}
		return result;
	}

	public QueryResultList combine(QueryResultList other) {
		Preconditions.checkNotNull(other);
		List<QueryResultItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		QueryResultList result = new QueryResultList(combinations + other.combinations);
		while (i1 < items.size() && i2 < items2.size()) {
			QueryResultItem p1 = items.get(i1);
			QueryResultItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				double score = p1.getScore() + MISSING_SCORE * other.combinations;
				result.addItem(new QueryResultItem(p1.getItem(), score, p1.getSnippet()));
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem merged = p1.getItem().merge(p2.getItem());
				double score = p1.getScore() + p2.getScore();
				String snippet = p1.getSnippet() != null ? p1.getSnippet() : p2.getSnippet();
				result.addItem(new QueryResultItem(merged, score, snippet));
				i1++;
				i2++;
			} else {
				double score = p2.getScore() + MISSING_SCORE * combinations;
				result.addItem(new QueryResultItem(p2.getItem(), score, p2.getSnippet()));
				i2++;
			}
		}
		while (i1 < items.size()) {
			QueryResultItem p1 = items.get(i1++);
			result.addItem(new QueryResultItem(p1.getItem(), p1.getScore() + MISSING_SCORE * other.combinations, p1.getSnippet()));
		}
		while (i2 < items2.size()) {
			QueryResultItem p2 = items2.get(i2++);
			result.addItem(new QueryResultItem(p2.getItem(), p2.getScore() + MISSING_SCORE * combinations, p2.getSnippet()));
		}
		return result;
	}

	public QueryResultList combinePhrase(QueryResultList QueryResultList) {
		Preconditions.checkNotNull(QueryResultList);
		List<QueryResultItem> items2 = QueryResultList.items;
		int i1 = 0, i2 = 0;
		QueryResultList result = new QueryResultList(combinations);
		while (i1 < items.size() && i2 < items2.size()) {
			QueryResultItem p1 = items.get(i1);
			QueryResultItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem combined = p1.getItem().combinePhrase(p2.getItem());
				if (combined.getPositions().size() > 0) {
					String snippet = p1.getSnippet() != null ? p1.getSnippet() : p2.getSnippet();
					result.addItem(new QueryResultItem(combined, 0, snippet));
				}
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return result;
	}

	public void calculateTfidfScores(double weight, int totalDocumentCount) {
		double idf = Math.log(totalDocumentCount / (double) items.size());

		for (QueryResultItem item : items) {
			double boost = 1;
			if (item.hasTermInTitle()) {
				boost = 3;
			} else if (item.hasTermInAbstract()) {
				boost = 2;
			}
			item.setScore(Math.log(item.getItem().getTermFrequency() * idf) * weight * boost);
		}
	}

	public List<QueryResultItem> getItems() {
		return items;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("items", items)
				.add("combinations", combinations)
				.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof QueryResultList)) return false;
		QueryResultList that = (QueryResultList) o;
		return combinations == that.combinations &&
				Objects.equal(items, that.items);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(items, combinations);
	}
}
