package de.hpi.ir.bingo.queries;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Lists;

import de.hpi.ir.bingo.PostingList;
import de.hpi.ir.bingo.PostingListItem;

public class QueryResultList {
	public static final Comparator<? super QueryResultItem> SCORE_COMPARATOR = Comparator.comparing(QueryResultItem::getScore).reversed();

	private static final double MISSING_SCORE = Math.log(1.0 / 100_000);

	private final List<QueryResultItem> items;

	// number of QueryResultLists that were combined to create this one
	private final int combinations;

	public QueryResultList(PostingList postingList) {
		this(Lists.newArrayListWithCapacity(postingList.getItems().size()), 1);
		for (PostingListItem item : postingList.getItems()) {
			items.add(new QueryResultItem(item, 0));
		}
	}

	public QueryResultList() {
		this(1);
	}

	private QueryResultList(int combinations) {
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
				double score = Math.max(p1.getScore(), p2.getScore());
				result.addItem(new QueryResultItem(and, score));
				i1++;
				i2++;
			} else {
				i2++;
			}
		}
		return result;
	}

	public QueryResultList or(QueryResultList other) {
		Preconditions.checkNotNull(other);
		List<QueryResultItem> items2 = other.items;
		int i1 = 0, i2 = 0;
		QueryResultList result = new QueryResultList(combinations + other.combinations);
		while (i1 < items.size() && i2 < items2.size()) {
			QueryResultItem p1 = items.get(i1);
			QueryResultItem p2 = items2.get(i2);
			if (p1.getPatentId() < p2.getPatentId()) {
				result.addItem(p1);
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem or = p1.getItem().merge(p2.getItem());
				double score = Math.log(Math.exp(p1.getScore()) + Math.exp(p2.getScore()));
				result.addItem(new QueryResultItem(or, score));
				i1++;
				i2++;
			} else {
				result.addItem(p2);
				i2++;
			}
		}
		while (i1 < items.size()) {
			result.addItem(items.get(i1++));
		}
		while (i2 < items2.size()) {
			result.addItem(items2.get(i2++));
		}
		return result;
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
				double score = p1.getScore() + MISSING_SCORE*other.combinations;
				result.addItem(new QueryResultItem(p1.getItem(), score));
				i1++;
			} else if (p1.getPatentId() == p2.getPatentId()) {
				PostingListItem merged = p1.getItem().merge(p2.getItem());
				double score = p1.getScore() + p2.getScore();
				result.addItem(new QueryResultItem(merged, score));
				i1++;
				i2++;
			} else {
				double score = p2.getScore() + MISSING_SCORE*combinations;
				result.addItem(new QueryResultItem(p2.getItem(), score));
				i2++;
			}
		}
		while (i1 < items.size()) {
			QueryResultItem p1 = items.get(i1++);
			result.addItem(new QueryResultItem(p1.getItem(),p1.getScore() + MISSING_SCORE*other.combinations ));
		}
		while (i2 < items2.size()) {
			QueryResultItem p2 = items2.get(i2++);
			result.addItem(new QueryResultItem(p2.getItem(), p2.getScore() + MISSING_SCORE*combinations));
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
				PostingListItem union = p1.getItem().combinePhrase(p2.getItem());
				if (union.getPositions().size() > 0) {
					result.addItem(new QueryResultItem(union, 0));
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
		for (QueryResultItem item : items) {
			item.setScore(Math.log(tfidf(this, item, totalDocumentCount) * weight));
		}
	}

	private double tfidf(QueryResultList queryResultList, QueryResultItem item, int totalDocumentCount) {
		return item.getItem().getTermFrequency() * Math.log(totalDocumentCount / (double) queryResultList.getItems().size());
	}

	public List<QueryResultItem> getItems() {
		return items;
	}
}
